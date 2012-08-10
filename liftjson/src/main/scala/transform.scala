package jp.as.lift

import jp.{
  PathElement, Root, NamedProperty, AnyProperty,
  ArrayAccess, ArraySlice, All, Segment,
  Transform
}
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonParser

object Json extends Transform[String, JValue] {

  def apply(in: String, path: List[PathElement]): JValue =
    filter(JsonParser.parse(in), path)

  def filter(value: JValue, path: List[PathElement]): JValue =
    path match {
      case Nil => value
      case el :: els =>
        filter(filter(value, el), els)
    }

  def filter(value: JValue, el: PathElement): JValue =
    (el match {
      case NamedProperty(false, name) =>
        (value \ name, value) match {
          case (res, _: JArray) =>
            res match {
              case JNothing =>
                JArray(List())
              case ary: JArray =>
                JArray(List(ary))
              case el =>
                el
            }
          case (jv, _) =>
            jv
        }
      case NamedProperty(true, name) =>
        value \\ name
      case AnyProperty(false) =>
        value
      case AnyProperty(true) =>
        value
      case ArrayAccess(prop, slice) =>
        filter(filter(value, prop), slice)
      case All =>
        value match {
          case ary: JArray => ary
          case _ => JNothing
        }
      case Segment(start, end, step) =>
        value match {
          case ary: JArray =>
            val slice = ary.arr.slice(start, end)
            JArray(
              if (step != 1) slice.grouped(step).toList.map(_.head)
              else slice
            )
          case _ =>
            JNothing
        }
      case Root =>
        value
      case _ =>
        JNothing        
    }) match {
      case JField(_, value) =>
        value
      case JArray(ary) =>
        JArray(ary map {
          case JField(_, value) => value
          case e => e
        })
      case jv =>
        jv
    }
}
