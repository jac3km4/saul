package saul
import cats.syntax.all._

trait UrlParser[A] {
  def parse(str: String): Option[A]
}

object UrlParser {
  implicit val stringParser: UrlParser[String] =
    _.some

  implicit val boolParser: UrlParser[Boolean] =
    str => Either.catchNonFatal(str.toBoolean).toOption
}
