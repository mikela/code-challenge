package controllers

import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.libs.json.Json
import play.api.Logger

import scala.collection.mutable
import scala.util.Sorting

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Mikel Anabitarte"))
  }

  val inputLineForm: Form[InputLine] = Form {
    mapping(
      "inputData" -> text)(InputLine.apply)(InputLine.unapply)
  }

  def addInputLine() = Action { implicit request =>
    val inputLine = inputLineForm.bindFromRequest.get
    DB.save(inputLine)
    Redirect(routes.Application.index())
  }

  def deleteLast() = Action {
    val inputLines = DB.query[InputLine].fetch()
    if (inputLines.nonEmpty) {
      DB.delete(inputLines.last)
    }
    Redirect(routes.Application.index())
  }

  /**
   *  Given two arrays with login and logout times, return a Solution with:
   *  - An array that counts the overlaps of those ranges, to draw the graph (x, y values).
   *  - The oldest login time, to make it the origin of the x axis.
   *  - A list with the "most used" time periods.
   * @param openings An array with login times.
   * @param closings An array with logout times.
   * @return A Solution object, with a "head" > -1 if it has drawable data.
   */
  private def countIntersection(openings:Array[Int], closings:Array[Int]): Solution = {
    val solutionY = new Array[Int](closings.last - openings.head + 1)
    var countUsage, opCounter, clCounter, maxUsageCounter = 0
    val maxUsageList = new mutable.MutableList[Array[Int]]
    for (i <- solutionY.indices) {
      while (opCounter < openings.length && i + openings.head == openings(opCounter)) {
        countUsage += 1
        opCounter += 1
      }
      while (clCounter < closings.length && i + openings.head == closings(clCounter)) {
        countUsage -= 1
        clCounter += 1
      }
      if (maxUsageCounter < countUsage) {
        maxUsageCounter = countUsage
        maxUsageList.clear()
        maxUsageList += Array(i + openings.head, -1)
      } else if (maxUsageCounter == countUsage) {
        // 2 cases:
        // 1. We are in a new equal high; we add it to the array
        // 2. We continue the new high (nothing to do) AND it's the end of the array solutionY
        if (maxUsageList.last(1) != -1) {
          maxUsageList += Array(i + openings.head, -1)
        } else {
          if (i == solutionY.length - 1) {
            maxUsageList.last(1) = i + openings.head
          }
        }
      } else {
        if (solutionY(i - 1) == maxUsageCounter) {
          maxUsageList.last(1) = i + openings.head
        }
      }
      solutionY(i) = countUsage
    }
    Solution(solutionY, openings.head, maxUsageList)
  }

  /**
   * Retrieve the last set of data the user submitted and send the data required to draw the graph (x axis, y axis etc).
   * @return A Solution in JSON format. If there was no data in the DB the "head" will be -1.
   */
  def getInputLines = Action {
    var sol:Solution = null
    val inputLines = DB.query[InputLine].fetch()
    if (inputLines.nonEmpty) {
      val last = inputLines.last.inputData.dropWhile(a => a != '(').drop(1)
      val array = last.split("\\)\r\n\\(|\\(|\\)|,").map(_.trim)

      val numLines = array.length / 3
      val openings = new Array[Int](numLines)
      val closings = new Array[Int](numLines)
      var j = 0
      for (i <- array.indices
           if i % 3 == 0) {
        openings(j) = array(i).toInt
        closings(j) = array(i + 1).toInt
        j += 1
      }

      Sorting.quickSort(openings)
      Sorting.quickSort(closings)
      sol = countIntersection(openings, closings)
      Logger.info("DB has data.")

    } else {
      Logger.info("DB is empty.")
      sol = new Solution(Array[Int](-1), -1, new mutable.MutableList[Array[Int]])
    }
    Ok(Json.toJson(sol))
  }
}
