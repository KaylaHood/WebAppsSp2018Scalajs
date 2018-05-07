package canvas

import org.scalajs.dom
import dom.document
import scala.scalajs.js.annotation.JSExportTopLevel
import org.querki.jquery._
import play.api.libs.json._
import scala.scalajs.js.JSON
import scala.scalajs.js
import org.scalajs.dom.html
import scala.scalajs.js.annotation.JSGlobal
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._
import sharedModels._
  
object CanvasMain {
  var socket: org.scalajs.dom.raw.WebSocket = null
  var socketTimer: scala.scalajs.js.timers.SetTimeoutHandle = null
  var socketIsInitialized: Boolean = false;
  
  var canvas: html.Canvas = null
  var ctx: dom.CanvasRenderingContext2D = null
  
  var rects: ArrayBuffer[Rectangle] = ArrayBuffer()
  var circles: ArrayBuffer[Circle] = ArrayBuffer()
  var lines: ArrayBuffer[Line] = ArrayBuffer()
  
  var selfId: Int = -1
  
  val colors: List[String] = List("Red","Orange","Yellow","Green","Cyan","Blue","Purple","SaddleBrown","White","Black")
  var curColorIdx: Int = 0
    
  @JSExportTopLevel("CanvasMain")
  def main(myCanvas: html.Canvas): Unit = {
    initWebSocket()
    keepWebSocketAlive()
    
    canvas = myCanvas
    ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    dom.window.onresize = (e : dom.Event) => { resizeCanvas() }
    
    rectDrawMode()
    
    canvas.setAttribute("tabIndex", "0")
    canvas.focus()
    
    canvas.onkeypress = {
      (e: dom.KeyboardEvent) => {
        dom.console.log("window received onkeydown event with keyCode: " + e.keyCode)
        if(e.keyCode == 114) {
          // R key
          rectDrawMode()
        } else if(e.keyCode == 99) {
          // C key
          circleDrawMode()
        } else if(e.keyCode == 108) {
          // L key
          lineDrawMode()
        } else if(e.keyCode == 120) {
          // X key
          clearShapes()
        } else if(e.keyCode == 32) {
          // spacebar
          curColorIdx = curColorIdx + 1
        }
      }
    }
    
    resizeCanvas()
    
    js.timers.setInterval(80){drawCanvas()}
  }
  
  var rectX1: Double = 0
  var rectY1: Double = 0
  
  def rectDrawMode(): Unit = {
    drawModeText = { 
      () => {
        ctx.font = "18px Papyrus San MS"
        ctx.fillStyle = "black"
        ctx.fillText("Rectangle Mode", 5, canvas.height - 5, 300)
      }
    }
    
    rectOnClick1()
  }
  
  def rectOnClick1(): Unit = {
    canvas.onclick = (e: dom.MouseEvent) => {
      //dom.console.log("rectOnClick1 at x: " + e.pageX.toString() + ", y: " + e.pageY.toString())
      rectX1 = e.pageX - (canvas.clientLeft)
      rectY1 = e.pageY - (canvas.clientTop)
      rectOnClick2()
    }
  }
  
  def rectOnClick2(): Unit = {
    canvas.onclick = (e: dom.MouseEvent) => {
      //dom.console.log("rectOnClick2 at x: " + e.pageX.toString() + ", y: " + e.pageY.toString())
      val x2 = e.pageX - (canvas.clientLeft)
      val y2 = e.pageY - (canvas.clientTop)
      val x1 = if (rectX1 > x2) x2 else rectX1
      val y1 = if (rectY1 > y2) y2 else rectY1 
      val width: Double = Math.abs(x2 - rectX1)
      val height: Double = Math.abs(y2 - rectY1)
      val r = Rectangle(selfId, colors(curColorIdx), x1, y1, width, height)
      addRectWS(r)
      rectOnClick1()
    }
  }
  
  var circX1: Double = 0
  var circY1: Double = 0
  
  def circleDrawMode(): Unit = {
    drawModeText = { 
      () => {
        ctx.font = "18px Papyrus San MS"
        ctx.fillStyle = "black"
        ctx.fillText("Circle Mode", 5, canvas.height - 5, 300)
      }
    }
    
    circleOnClick1()
  }
  
  def circleOnClick1(): Unit = {
    canvas.onclick = (e: dom.MouseEvent) => {
      //dom.console.log("circleOnClick1 at x: " + e.pageX.toString() + ", y: " + e.pageY.toString())
      circX1 = e.pageX - (canvas.clientLeft)
      circY1 = e.pageY - (canvas.clientTop)
      circleOnClick2()
    }
  }
  
  def circleOnClick2(): Unit = {
    canvas.onclick = (e: dom.MouseEvent) => {
      //dom.console.log("circleOnClick2 at x: " + e.pageX.toString() + ", y: " + e.pageY.toString())
      val x2 = e.pageX - (canvas.clientLeft)
      val y2 = e.pageY - (canvas.clientTop)
      val xCenter = x2 - ((x2 - circX1)/2.0)
      val yCenter = y2 - ((y2 - circY1)/2.0)
      val dist = Math.sqrt(Math.pow(x2 - circX1, 2) + Math.pow(y2 - circY1, 2))
      val c = Circle(selfId, colors(curColorIdx), xCenter, yCenter, dist / 2.0)
      addCircleWS(c)
      circleOnClick1()
    }
  }
  
  var lineX1: Double = 0
  var lineY1: Double = 0
  
  def lineDrawMode(): Unit = {
    drawModeText = { 
      () => {
        ctx.font = "18px Papyrus San MS"
        ctx.fillStyle = "black"
        ctx.fillText("Line Mode", 5, canvas.height - 5, 300)
      }
    }
    
    lineOnClick1()
  }
  
  def lineOnClick1(): Unit = {
    canvas.onclick = (e: dom.MouseEvent) => {
      //dom.console.log("lineOnClick1 at x: " + e.pageX.toString() + ", y: " + e.pageY.toString())
      lineX1 = e.pageX - (canvas.clientLeft)
      lineY1 = e.pageY - (canvas.clientTop)
      lineOnClick2()
    }
  }
  
  def lineOnClick2(): Unit = {
    canvas.onclick = (e: dom.MouseEvent) => {
      //dom.console.log("lineOnClick2 at x: " + e.pageX.toString() + ", y: " + e.pageY.toString())
      val x2 = e.pageX - (canvas.clientLeft)
      val y2 = e.pageY - (canvas.clientTop)
      val ln = Line(selfId, colors(curColorIdx), lineX1, lineY1, x2, y2)
      addLineWS(ln)
      lineOnClick1()
    }
  }
  
  def drawRect(rect: Rectangle): Unit = {
    ctx.fillStyle = rect.color
    ctx.fillRect(rect.x, rect.y, rect.width, rect.height)
  }
  
  def drawCircle(circ: Circle): Unit = {
    ctx.strokeStyle = circ.color
    ctx.lineWidth = 10
    ctx.beginPath()
    ctx.arc(circ.x, circ.y, circ.radius, 0, 2 * Math.PI)
    ctx.stroke()
  }
  
  def drawLine(line: Line): Unit = {
    ctx.strokeStyle = line.color
    ctx.lineWidth = 10
    ctx.beginPath()
    ctx.moveTo(line.x, line.y)
    ctx.lineTo(line.endX, line.endY)
    ctx.stroke()
  }
  
  var drawModeText: js.Function0[Unit] = { 
    () => {
      ctx.font = "18px Papyrus San MS"
      ctx.fillStyle = "black"
      ctx.fillText("Rectangle Mode", 5, canvas.height - 5, 300)
    }
  }

  def drawUI(): Unit = {
    ctx.font = "30px Papyrus San MS"
    ctx.fillStyle = "black"
    ctx.fillText("R = draw rectangles, C = draw circles, L = draw lines, X = clear canvas, Space = cycle colors", 5, 25, 900)
    drawModeText()
    ctx.font = "18px Papyrus San MS"
    ctx.fillText("Color", canvas.width - 60, canvas.height - 85, 50)
    ctx.fillStyle = colors(curColorIdx)
    ctx.strokeStyle = "black"
    ctx.lineWidth = 15
    ctx.fillRect(canvas.width - 70, canvas.height - 70, 60, 60)
    ctx.strokeRect(canvas.width - 70, canvas.height - 70, 60, 60)
    ctx.strokeStyle = "white"
    ctx.lineWidth = 5
    ctx.strokeRect(canvas.width - 70, canvas.height - 70, 60, 60)
  }
  
  def clearCanvas(): Unit = {
    ctx.clearRect(0, 0, canvas.clientWidth, canvas.clientHeight)
    ctx.fillStyle = "white"
    ctx.fillRect(0, 0, canvas.width, canvas.height)
  }
  
  def clearShapes(): Unit = {
    rects.clear()
    circles.clear()
    lines.clear()
  }
  
  def drawCanvas(): Unit = {
    clearCanvas()
    drawUI()
    if (rects.length > 0) {
      for (i <- 0 until rects.length) {
        var rOpt = rects.lift(i)
        if(rOpt.isDefined) {
          drawRect(rOpt.get)
        }
      }
    }
    if (circles.length > 0) {
      for (i <- 0 until circles.length) {
        var cOpt = circles.lift(i)
        if(cOpt.isDefined) {
          drawCircle(cOpt.get)
        }
      }
    }
    if (lines.length > 0) {
      for (i <- 0 until lines.length) {
        var lOpt = lines.lift(i)
        if(lOpt.isDefined) {
          drawLine(lOpt.get)
        }
      }
    }
  }
  
  def resizeCanvas(): Unit = {
    var canvasHolderBounds = dom.window.document.getElementById("canvasHolder").getBoundingClientRect()
    canvas.width = canvasHolderBounds.width.toInt
    canvas.height = canvasHolderBounds.height.toInt
    drawCanvas()
  }
  
  def addRect(r: Rectangle): Unit = {
    rects.append(r)
  }
  
  def doesRectExist(rThat: Rectangle): Boolean = {
    rects.exists(rThis => {
      rThis.x == rThat.x && rThis.y == rThat.y && rThis.width == rThat.width && rThis.height == rThat.height && rThis.color == rThat.color
    })
  }
  
  def addCircle(c: Circle): Unit = {
    circles.append(c)
  }
  
  def doesCircleExist(cThat: Circle): Boolean = {
    circles.exists(cThis => {
      cThis.x == cThat.x && cThis.y == cThat.y && cThis.radius == cThat.radius && cThis.color == cThat.color
    })
  }
  
  def addLine(ln: Line): Unit = {
    lines.append(ln)
  }
  
  def doesLineExist(lnThat: Line): Boolean = {
    lines.exists(lnThis => {
      lnThis.x == lnThat.x && lnThis.y == lnThat.y && lnThis.endX == lnThat.endX && lnThis.endY == lnThat.endY && lnThis.color == lnThat.color
    })
  }
  
  def addRectWS(rect: Rectangle): Unit = {
    val rectMsg: Message = AddShapeMessage(rect)
    sendOverWS(rectMsg)
  }
  
  def addCircleWS(circle: Circle): Unit = {
    val circMsg: Message = AddShapeMessage(circle)
    sendOverWS(circMsg)
  }
  
  def addLineWS(line: Line): Unit = {
    val lineMsg: Message = AddShapeMessage(line)
    sendOverWS(lineMsg)
  }
  
  def sendOverWS(msg: Message): Unit = {
    if(socketIsInitialized && socketTimer != null) {
      socket.send(Json.stringify(Json.toJson(msg)))
    }
  }
  
  def initWebSocket(): Unit = {
    socket = new dom.WebSocket("ws://" + dom.window.location.hostname + ":" + dom.window.location.port + "/socket")
    socket.onopen = {
      (e: dom.Event) =>
        val nMsg: Message = NullMessage(Some(selfId))
        socket.send(Json.stringify(Json.toJson(nMsg)))
    }
    socket.onmessage = {
      (e: dom.MessageEvent) =>
        val msg = Json.parse(e.data.toString()).validate[Message]
        msg match {
          case success: JsSuccess[Message] => {
            val result = success.get
            result match {
              case a: AddShapeMessage => {
                val shape = a.shape
                shape match {
                  case r: Rectangle => {
                    addRect(r)
                  }
                  case c: Circle => {
                    addCircle(c)
                  }
                  case l: Line => {
                    addLine(l)
                  }
                }
              }
              case s: SyncCanvasMessage => {
                val shapes = s.canvas.shapes.reverse
                clearShapes()
                shapes.foreach(s => {
                  s match {
                    case r: Rectangle => {
                      addRect(r)
                    }
                    case c: Circle => {
                      addCircle(c)
                    }
                    case ln: Line => {
                      addLine(ln)
                    }
                  }
                })
              }
              case u: RemoveShapesWithUserIdMessage => {
                rects = rects.filterNot(r => r.userId == u.userId)
                circles = circles.filterNot(c => c.userId == u.userId)
                lines = lines.filterNot(ln => ln.userId == u.userId)
              }
              case i: InitSelfMessage => {
                selfId = i.selfId
              }
              case n: NullMessage => {
                // do nothing
              }
              case _ => {
                // do nothing
              }
            }
          }
          case error: JsError => {
            // do nothing
          }
        }
    }
    socketIsInitialized = true
  }
  
  def keepWebSocketAlive(): Unit = {
    var timeout: Double = 15000;
    if(socket.readyState == org.scalajs.dom.raw.WebSocket.OPEN) {
      val nmsg: Message = NullMessage(Some(selfId))
      socket.send(Json.stringify(Json.toJson(nmsg)))
    } else if(
        socket.readyState == org.scalajs.dom.raw.WebSocket.CLOSED 
        || socket.readyState == org.scalajs.dom.raw.WebSocket.CLOSING) {
      socketIsInitialized = false
      socket.close()
      initWebSocket()
    }
    socketTimer = js.timers.setTimeout(timeout) {
      keepWebSocketAlive()
    }
  }
  
  def cancelKeepWebSocketAlive(): Unit = {
    if(socketTimer != null) {
      js.timers.clearTimeout(socketTimer)
      socketIsInitialized = false
    }
  }

}