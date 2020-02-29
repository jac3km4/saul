package object saul {
  final class Combine[A, B]
  final class Segment[A]
  final class Query[R, A]
  final class Param[S, A]
  final class Body[A]
  final class Or[A, B]
  final class To[R, A]

  type Service[R] = Request => Option[R]
}
