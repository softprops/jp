# jp

A simple [json-path][jsonpath] parser for scala

# install

...

# usage

## Parsing paths

The core functionality exposed in `jp` is json path parsing

```scala
import jp._

val path = Path("$.store.book[0].title")
```

Will return a `Path` object from which you may call `result` to get the
corresponding [ParseResult][parseresult].

## Transforming paths

To make results more useful in applications, `jp` exposes a trait called `Transform[I,O]`
which transforms a type `I` into type `O` given the json path parse results.

Below is an example of transforming a json path parse result with lift json.

```scala
import net.liftweb.json.JsonAST
import net.liftweb.json.Printer

path("""{"store":"book":[{"title":"test"}]}""")(as.lift.Json).fold(identity, { js =>
   Printer.pretty(JsonAST.render(js))
})
```

Passing in a `Transform` will result in `Either[String, T]` where String contains an error
message for a potential malform parsing result, and `T` represents the type of the transformation,
in this, a case `JValue`.

# reference

<table>
  <tr>
    <th>JSONPath Operator</th><th>Description</th>
  </tr>
  <tr>
    <td>$</td><td>the root object/element</td>
  </tr>
  <tr>
    <td>@</td><td>the current object/element</td>
  </tr>
  <tr>
    <td>. or []</td><td>child operator</td>
  </tr>
  <tr>
    <td>..</td><td>recursive descent. JSONPath borrows this syntax from E4X</td>
  </tr>
  <tr>
    <td>*</td><td>wildcard. All objects/elements regardless their names.</td>
  </tr>
  <tr>
    <td>[]</td><td>subscript operator.</td>
  </tr>
  <tr>
    <td>[,]</td><td>Union operator in XPath results in a combination of node sets. JSONPath allows alternate names or array indices as a set.</td>
   </tr>
   <tr>
    <td>[start:end:step]</td><td>array slice operator borrowed from ES4</td>
    </tr>
   <tr>
    <td>?()</td><td>applies a filter (script) expression</td>    
    </tr>
   <tr>
    <td>()</td><td>script expression, using the underlying script engine</td>
  </tr>  
</table>

# todo

- `(<expr>)` expression evaluation `$.store.book[(@.length-1)]`
- `@` current object references (only useful in `(<expr>)` contexts)
- `?(<bool expr>)` filters `$.store.book[?(@.price < 10)].title`
- `[,]` union operator `$..book[0,1]`

Doug Tangren (softprops) 2012

[jsonpath]: http://goessner.net/articles/JsonPath/
[parseresult]: http://www.scala-lang.org/api/current/scala/util/parsing/combinator/Parsers$ParseResult.html
