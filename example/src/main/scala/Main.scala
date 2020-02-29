import saul.dsl._
import saul.{Method, Request, Servable}

object Main {

  def main(args: Array[String]): Unit = {
    sealed trait UserRequest                                                  extends Product with Serializable
    final case class DeleteProfile(email: String)                             extends UserRequest
    final case class GetProfile(userId: String, includeEntitlements: Boolean) extends UserRequest

    type Api =
      (GET / "users" / path[String] ? param["include-entitlements", Boolean] to GetProfile) <|>
        (DELETE / "users" / path[String] to DeleteProfile)

    val api = Servable[Api]

    val service = api.serve { req: UserRequest =>
      req match {
        case DeleteProfile(userId)                   => "not ok"
        case GetProfile(userId, includeEntitlements) => "ok"
      }
    }

    val res = service(Request(Method.Get, "/users/1", Map("include-entitlements" -> "true"), Some("body"), List.empty))
    println(res)
  }
}
