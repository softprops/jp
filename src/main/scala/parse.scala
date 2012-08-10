package jp

import scala.util.parsing.combinator.JavaTokenParsers

object Parse extends JavaTokenParsers {

  private val Access = "."

  private val Descendant = ".."

  private val Wildcard = "*"

  private val Dollar = "$"

  private val JsonIdentifier = "[$a-zA-Z_][0-9a-zA-Z_$]*".r

  def path: Parser[List[PathElement]] =
    root ~ rep(segment) ^^ 
      { case root ~ segment => root :: segment }
    
  def segment: Parser[PathElement] = (
    subArrayAccess
    | dotProperty
    | descendantProperty
    | dotAny
    | descendantAny
    | subscriptProperty)

  def anyField: Parser[Property] = Wildcard ^^ (_ => AnyProperty())

  def root: Parser[PathElement] =  complexRoot | bareRoot

  def bareRoot: Parser[Property] = Dollar ^^ (_ => Root)

  def complexRoot: Parser[ArrayAccess] = 
    Dollar ~> arraySlice ^^ ((slice) => ArrayAccess(Root, slice))

  def property: Parser[NamedProperty] =
    jsonident ^^ (NamedProperty(false, _))

  // jtp ident's don't permit digits
  def jsonident: Parser[String] =
     JsonIdentifier

  def subscriptProperty: Parser[Property] = 
    "['" ~> property <~ "']"

  def dotProperty: Parser[Property] =
    Access ~> property

  def descendantProperty: Parser[Property] =
    Descendant ~> property ^^ (p => NamedProperty(true, p.name))

  def dotAny: Parser[AnyProperty] =
    Access + Wildcard ^^ (_ => AnyProperty(false))

  def descendantAny: Parser[AnyProperty] =
    Descendant + Wildcard ^^ (_ => AnyProperty(true))

  def arraySlice: Parser[ArraySlice] =
    arraySlice1 | globArraySlice

  def arraySlice1: Parser[Segment] =
    "[" ~> wholeNumber <~ "]" ^^ (
      start => Segment(start.toInt, start.toInt + 1, 1)
     )

  def globArraySlice: Parser[ArraySlice] =
    "[" ~> Wildcard <~ "]" ^^ (_ => All)

  def arrayFieldAccess: Parser[ArrayAccess] =
    property ~ arraySlice ^^ {
      case property ~ slice => ArrayAccess(property, slice)
    }

  def deepArrayFieldAccess: Parser[ArrayAccess] =
    Descendant ~> (property ~ arraySlice) ^^ {
      case NamedProperty(_, name) ~ slice => ArrayAccess(NamedProperty(true, name), slice)
    }

  def arraySubscriptAccess: Parser[ArrayAccess] =
    subscriptProperty ~ arraySlice ^^ {
      case property ~ slice => ArrayAccess(property, slice)
    }

  def arrayAccess: Parser[ArrayAccess] = 
    arrayFieldAccess | arraySubscriptAccess

  // .name[index] or ['name'][index] for subacces, for example
  // $.name[1] or $['name'][0]
  def subArrayAccess: Parser[ArrayAccess] = 
    (Access ~> arrayFieldAccess) | arraySubscriptAccess | deepArrayFieldAccess

  def apply(spec: String): ParseResult[List[PathElement]] =
    parse(path, spec)
}
