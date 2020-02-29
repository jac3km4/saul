package saul

final case class Response(status: Int, body: Option[String], headers: List[Header])
