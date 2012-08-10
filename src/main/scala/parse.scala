package jp

import scala.util.parsing.combinator.JavaTokenParsers

object Parse extends JavaTokenParsers {

  private val Access = "."

  private val Descendant = ".."

  private val Wildcard = "*"

  private val Dollar = "$"

  private val JsonIdentifier = "[$a-zA-Z_][0-9a-zA-Z_$]*".r

  /** base parser for Paths */
  def path: Parser[List[PathElement]] =
    root ~ rep(segment) ^^ 
      { case root ~ segment => root :: segment }
    
  /** elements of a path query */
  def segment: Parser[PathElement] = (
    subArrayAccess
    | dotProperty
    | descendantProperty
    | dotAny
    | descendantAny
    | subscriptProperty)

  /** wild card field */
  def anyField: Parser[Property] = Wildcard ^^ (_ => AnyProperty())

  /** root of path */
  def root: Parser[PathElement] =  complexRoot | bareRoot

  /** The similest root that can be defined */
  def bareRoot: Parser[Property] = Dollar ^^ (_ => Root)

  /** A root and access to its members */
  def complexRoot: Parser[ArrayAccess] = 
    Dollar ~> arraySlice ^^ ((slice) => ArrayAccess(Root, slice))

  /** a named json propery */
  def property: Parser[NamedProperty] =
    jsonident ^^ (NamedProperty(false, _))

  /** legal json identifier. jtp ident's don't permit digits */
  def jsonident: Parser[String] =
     JsonIdentifier

  /** a property accessed between square brackets */
  def subscriptProperty: Parser[Property] = 
    "['" ~> property <~ "']"

  /** a property accessed by dot reference */
  def dotProperty: Parser[Property] =
    Access ~> property

  /** a named "descendant" propery */
  def descendantProperty: Parser[Property] =
    Descendant ~> property ^^ (p => NamedProperty(true, p.name))

  /** dot reference to any property */
  def dotAny: Parser[AnyProperty] =
    Access + Wildcard ^^ (_ => AnyProperty(false))

  /** descendant referance to any property */
  def descendantAny: Parser[AnyProperty] =
    Descendant + Wildcard ^^ (_ => AnyProperty(true))

  /** any slice of a json array */
  def arraySlice: Parser[ArraySlice] =
    arrayIndex | fullArray

  /** reference to an index an array */
  def arrayIndex: Parser[Segment] =
    "[" ~> wholeNumber <~ "]" ^^ (
      start => Segment(start.toInt, start.toInt + 1, 1)
     )
  
  /** reference to an entire array */
  def fullArray: Parser[ArraySlice] =
    "[" ~> Wildcard <~ "]" ^^ (_ => All)

  /** access to a slice of an array */
  def arrayElementAccess: Parser[ArrayAccess] =
    property ~ arraySlice ^^ {
      case property ~ slice => ArrayAccess(property, slice)
    }

  /** descendant access to a slice of an array */
  def descendantArrayElementAccess: Parser[ArrayAccess] =
    Descendant ~> (property ~ arraySlice) ^^ {
      case NamedProperty(_, name) ~ slice =>
        ArrayAccess(NamedProperty(true, name), slice)
    }

  def arraySubscriptAccess: Parser[ArrayAccess] =
    subscriptProperty ~ arraySlice ^^ {
      case property ~ slice => ArrayAccess(property, slice)
    }

  def arrayAccess: Parser[ArrayAccess] = 
    arrayElementAccess | arraySubscriptAccess

  def subArrayAccess: Parser[ArrayAccess] = 
    (Access ~> arrayElementAccess) | arraySubscriptAccess | descendantArrayElementAccess

  /** given a path specficification, parse out a list of path elements */
  def apply(spec: String): ParseResult[List[PathElement]] =
    parse(path, spec)
}
