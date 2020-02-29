# saul
Write Scala APIs with types.
This library allows you to derive HTTP routing for services from nothing else but type definitions (kind of like Haskell servant).

# example

simple usage:
```scala
type Endpoint = POST / "users" / path[String] as User

val api =
  Servable[Endpoint].serve { (path: String, body: User) =>
    "ok"
  }

```

advanced usage with different routes and an ADT ([example](https://github.com/jac3km4/saul/blob/master/example/src/main/scala/Main.scala)):
```scala
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
```
