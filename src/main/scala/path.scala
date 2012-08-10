package jp

trait Transform[I,O] {
  def apply(in: I, path: List[PathElement]): O
}

class Path(spec: String) {
  import Parse.{ Error, Failure, Success }
  lazy val result = Parse(spec)
  def apply[I,O](in: I)(implicit trans: Transform[I,O]): Either[String, O] =
    result match {
      case Success(path, _) => Right(trans.apply(in, path))
      case Failure(msg, _) => Left(msg)
      case Error(msg, _) => Left(msg)
    }
}

object Path {
  def apply(spec: String) =
    new Path(spec)
}
