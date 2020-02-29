package saul

trait EntityParser[A] {
  def parse(req: Request): Option[A]
}

object EntityParser {
  implicit val stringParser: EntityParser[String] =
    _.body

  implicit val reqParser: EntityParser[Request] =
    req => Some(req)
}
