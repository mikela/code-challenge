package models

import play.api.libs.json.Json

case class Solution(solY:Array[Int], head:Int, maxUsageList:scala.collection.mutable.MutableList[Array[Int]])

object Solution {
  implicit val SolutionFormat = Json.format[Solution]
}
