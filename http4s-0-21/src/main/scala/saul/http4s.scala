package saul

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import fs2.{Chunk, Stream}
import org.http4s.{Headers, HttpRoutes, Status}

object http4s {
  def wrap[F[_]: Sync](service: Service[F[org.http4s.Response[F]]]): HttpRoutes[F] =
    HttpRoutes.apply[F] { req =>
      val method: Option[Method] = req.method match {
        case org.http4s.Method.GET     => Method.Get.some
        case org.http4s.Method.HEAD    => Method.Head.some
        case org.http4s.Method.POST    => Method.Post.some
        case org.http4s.Method.PUT     => Method.Put.some
        case org.http4s.Method.DELETE  => Method.Delete.some
        case org.http4s.Method.OPTIONS => Method.Options.some
        case org.http4s.Method.PATCH   => Method.Patch.some
        case _                         => none
      }

      for {
        body   <- OptionT.liftF(req.as[String])
        method <- OptionT.fromOption[F](method)
        headers = req.headers.toList.map(h => Header(h.name.value, h.value))
        request = Request(method, req.uri.path, req.uri.query.params, body.some, headers)
        response <- OptionT(service(request).sequence)
      } yield response
    }

  def routes[F[_]: Sync](service: Service[F[Response]]): HttpRoutes[F] =
    wrap { req: Request =>
      service(req).map(_.map { res =>
        val headers = Headers(res.headers.map(h => org.http4s.Header(h.name, h.value)))
        val body = Stream.chunk(Chunk.bytes(res.body.orEmpty.getBytes))
        org.http4s.Response[F](Status(res.status), headers = headers, body = body)
      })
    }
}
