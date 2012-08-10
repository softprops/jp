package jp

sealed trait PathElement

trait Property extends PathElement

case object Root extends Property

case class AnyProperty(desendent: Boolean = false) extends Property

case class NamedProperty(desendent: Boolean, name: String) extends Property

case class ArrayAccess(property: Property, slice: ArraySlice) extends PathElement

trait ArraySlice extends PathElement

case object All extends ArraySlice

case class Segment(start: Int, end: Int, step: Int = 1) extends ArraySlice
