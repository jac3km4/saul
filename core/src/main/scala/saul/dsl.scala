package saul

object dsl {
  type GET     = Method.Get.type
  type HEAD    = Method.Head.type
  type POST    = Method.Post.type
  type PUT     = Method.Put.type
  type DELETE  = Method.Delete.type
  type OPTIONS = Method.Options.type
  type PATCH   = Method.Patch.type

  type /[R, A]     = Combine[R, A]
  type ?[R, A]     = Combine[R, A]
  type <|>[A, B]   = Or[A, B]
  type to[R, A]    = To[R, A]
  type as[R, A]    = Combine[R, Body[A]]
  type path[A]     = Segment[A]
  type param[S, A] = Param[S, A]
}
