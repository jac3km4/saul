package saul

import cats.Functor
import cats.implicits._
import shapeless._
import shapeless.ops.function.FnToProduct
import shapeless.ops.hlist.Prepend

import scala.{:: => /::}

sealed trait Servable[A] {
  type Out

  def extract(req: Request, remainder: List[String]): Option[ExtractOutput[Out]]

  def serve[FN, R](f: FN)(implicit fnToProduct: FnToProduct.Aux[FN, Out => R]): Request => Option[R] = { req =>
    val path = req.path.stripPrefix("/").split('/').toList
    extract(req, path).map(res => fnToProduct.apply(f)(res.value))
  }
}

object Servable {
  type Aux[A, O] =
    Servable[A] {
      type Out = O
    }

  trait Single[A, O] extends Servable[A] {
    def extractSingle(req: Request, remainder: List[String]): Option[ExtractOutput[O]]

    override type Out = O :: HNil
    override def extract(req: Request, remainder: List[String]): Option[ExtractOutput[Out]] =
      extractSingle(req, remainder).map(_.map(_ :: HNil))
  }

  def apply[A](implicit servable: Servable[A]): servable.type = servable

  implicit def servableCombine[A, A0 <: HList, B, B0 <: HList, O <: HList](
      implicit servableA: Servable.Aux[A, A0],
      servableB: Servable.Aux[B, B0],
      prepend: Prepend.Aux[A0, B0, O]
  ): Servable.Aux[Combine[A, B], O] =
    new Servable[Combine[A, B]] {
      override type Out = O

      override def extract(req: Request, remainder: List[String]): Option[ExtractOutput[Out]] =
        servableA.extract(req, remainder).flatMap { res1 =>
          servableB.extract(req, res1.remainder).map(_.map(prepend(res1.value, _)))
        }
    }

  implicit def servableOr[A, A0, B, B0, C](
      implicit servableA: Servable.Aux[A, A0],
      servableB: Servable.Aux[B, B0],
      lub: Lub[A0, B0, C]
  ): Single[Or[A, B], C] =
    (req, remainder) =>
      servableA
        .extract(req, remainder)
        .map(_.map(lub.left))
        .orElse(servableB.extract(req, remainder).map(_.map(lub.right)))

  implicit def servableTo[A, A0 <: HList, C](
      implicit servable: Servable.Aux[A, A0],
      gen: Generic.Aux[C, A0]
  ): Servable.Aux[To[A, C], C] =
    new Servable[To[A, C]] {
      override type Out = C

      override def extract(req: Request, remainder: List[String]): Option[ExtractOutput[Out]] =
        servable.extract(req, remainder).map(res => ExtractOutput(gen.from(res.value), res.remainder))
    }

  implicit def servablePath[S <: String](implicit v: ValueOf[S]): Servable.Aux[S, HNil] =
    new Servable[S] {
      override type Out = HNil

      override def extract(req: Request, remainder: List[String]): Option[ExtractOutput[HNil]] =
        remainder match {
          case v.value /:: tail => Some(ExtractOutput(HNil, tail))
          case _                => None
        }
    }

  implicit def servableMethod[M <: Method](implicit valueOf: ValueOf[M]): Servable.Aux[M, HNil] =
    new Servable[M] {
      override type Out = HNil

      override def extract(req: Request, remainder: List[String]): Option[ExtractOutput[Out]] =
        if (req.method == valueOf.value) ExtractOutput(HNil, remainder).some else none
    }

  implicit def servableSegment[A](implicit parser: UrlParser[A]): Single[Segment[A], A] =
    (_, remainder) =>
      for {
        str <- remainder.headOption
        res <- parser.parse(str)
      } yield ExtractOutput(res, remainder.tail)

  implicit def servableParam[S <: String, A](
      implicit valueOf: ValueOf[S],
      parser: UrlParser[A]
  ): Single[Param[S, A], A] =
    (req, remainder) =>
      for {
        str <- req.query.get(valueOf.value)
        res <- parser.parse(str)
      } yield ExtractOutput(res, remainder)

  implicit def servableBody[A](implicit parser: EntityParser[A]): Single[Body[A], A] =
    (req, remainder) => parser.parse(req).map(value => ExtractOutput(value, remainder))
}

final case class ExtractOutput[+A](value: A, remainder: List[String])

object ExtractOutput {
  implicit val functor: Functor[ExtractOutput] = new Functor[ExtractOutput] {
    override def map[A, B](fa: ExtractOutput[A])(f: A => B): ExtractOutput[B] =
      ExtractOutput(f(fa.value), fa.remainder)
  }
}
