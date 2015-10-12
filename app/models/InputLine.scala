package models

import play.api.libs.json.Json

case class InputLine(inputData:String)

object InputLine {
  implicit val InputLineFormat = Json.format[InputLine]
}
