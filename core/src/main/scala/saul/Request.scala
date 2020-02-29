package saul

final case class Request(method: Method,
                         path: String,
                         query: Map[String, String],
                         body: Option[String],
                         headers: List[Header])

sealed trait Method

object Method {
  case object Get     extends Method
  case object Head    extends Method
  case object Post    extends Method
  case object Put     extends Method
  case object Delete  extends Method
  case object Options extends Method
  case object Patch   extends Method
}

final case class Header(name: String, value: String)
