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

@JSGlobal
@js.native
class JsRectangle (
    var x: Double,
    var y: Double,
    var halfWidth: Double,
    var halfHeight: Double
  ) extends js.Object
  
@JSGlobal
@js.native
class JsCircle (
    var x: Double,
    var y: Double,
    var radius: Double
  ) extends js.Object
  
@JSGlobal
@js.native
class JsLine (
    var x: Double,
    var y: Double,
    var endX: Double,
    var endY: Double
  ) extends js.Object
  
object CanvasMain {
  var socket: org.scalajs.dom.raw.WebSocket = null
  var socketTimer: scala.scalajs.js.timers.SetTimeoutHandle = null
  var socketIsInitialized: Boolean = false;
  
  var canvas: html.Canvas = null
  var ctx: dom.CanvasRenderingContext2D = null
  
  var rects: ArrayBuffer[JsRectangle] = ArrayBuffer()
  var circles: ArrayBuffer[JsCircle] = ArrayBuffer()
  var lines: ArrayBuffer[JsLine] = ArrayBuffer()
  
  var selfId: Int = -1
    
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
        //dom.console.log("window received onkeydown event with charCode: " + e.charCode)
        if(e.charCode == 'R' || e.charCode == 'r') {
          rectDrawMode()
        } else if(e.charCode == 'C' || e.charCode == 'c') {
          circleDrawMode()
        } else if(e.charCode == 'L' || e.charCode == 'l') {
          lineDrawMode()
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
        ctx.fillText("Rectangle Mode", 5, canvas.height, 300)
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
      val r = js.Dynamic.literal().asInstanceOf[JsRectangle]
      r.x = x2 - ((x2 - rectX1)/2.0)
      r.y = y2 - ((y2 - rectY1)/2.0)
      r.halfWidth = Math.abs(x2 - r.x)
      r.halfHeight = Math.abs(y2 - r.y)
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
        ctx.fillText("Circle Mode", 5, canvas.height, 300)
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
      val c = js.Dynamic.literal().asInstanceOf[JsCircle]
      c.x = x2 - ((x2 - circX1)/2.0)
      c.y = y2 - ((y2 - circY1)/2.0)
      var dist = Math.sqrt(Math.pow(x2 - circX1, 2) + Math.pow(y2 - circY1, 2))
      c.radius = dist/2.0
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
        ctx.fillText("Line Mode", 5, canvas.height, 300)
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
      val ln = js.Dynamic.literal().asInstanceOf[JsLine]
      ln.x = lineX1
      ln.y = lineY1
      ln.endX = x2
      ln.endY = y2
      addLineWS(ln)
      lineOnClick1()
    }
  }
  
  def drawRect(rect: JsRectangle): Unit = {
    ctx.fillStyle = "red"
    ctx.fillRect(rect.x - rect.halfWidth, rect.y - rect.halfHeight, rect.halfWidth * 2.0, rect.halfHeight * 2.0)
  }
  
  def drawCircle(circ: JsCircle): Unit = {
    ctx.strokeStyle = "blue"
    ctx.lineWidth = 10
    ctx.beginPath()
    ctx.arc(circ.x, circ.y, circ.radius, 0, 2 * Math.PI)
    ctx.stroke()
  }
  
  def drawLine(line: JsLine): Unit = {
    ctx.strokeStyle = "green"
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
      ctx.fillText("Rectangle Mode", 5, canvas.height, 300)
    }
  }

  def drawUI(): Unit = {
    ctx.font = "30px Papyrus San MS"
    ctx.fillStyle = "black"
    ctx.fillText("Press R to draw rectangles, C to draw circles, and L to draw lines", 5, 25, 550)
    drawModeText()
  }
  
  def clearCanvas(): Unit = {
    ctx.clearRect(0, 0, canvas.clientWidth, canvas.clientHeight)
    ctx.fillStyle = "white"
    ctx.fillRect(0, 0, canvas.width, canvas.height)
  }
  
  def drawCanvas(): Unit = {
    clearCanvas()
    drawUI()
    if (rects.length > 0) {
      for (i <- 0 until rects.length) {
        var r = rects.lift(i)
        if(r.isDefined) {
          drawRect(r.get)
        }
      }
    }
    if (circles.length > 0) {
      for (i <- 0 until circles.length) {
        var c = circles.lift(i)
        if(c.isDefined) {
          drawCircle(c.get)
        }
      }
    }
    if (lines.length > 0) {
      for (i <- 0 until lines.length) {
        var l = lines.lift(i)
        if(l.isDefined) {
          drawLine(l.get)
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
  
  def addRect(_x: Double, _y: Double, _halfWidth: Double, _halfHeight: Double): Unit = {
    var rect = js.Dynamic.literal().asInstanceOf[JsRectangle]
    rect.x = _x
    rect.y = _y
    rect.halfWidth = _halfWidth
    rect.halfHeight = _halfHeight
    rects.append(rect)
  }
  
  def doesRectExist(_x: Double, _y: Double, _halfWidth: Double, _halfHeight: Double): Boolean = {
    rects.exists(r => {
      r.x == _x && r.y == _y && r.halfWidth == _halfWidth && r.halfHeight == _halfHeight
    })
  }
  
  def addCircle(_x: Double, _y: Double, _radius: Double): Unit = {
    var circ = js.Dynamic.literal().asInstanceOf[JsCircle]
    circ.x = _x
    circ.y = _y
    circ.radius = _radius
    circles.append(circ)
  }
  
  def doesCircleExist(_x: Double, _y: Double, _radius: Double): Boolean = {
    circles.exists(c => {
      c.x == _x && c.y == _y && c.radius == _radius
    })
  }
  
  def addLine(_x: Double, _y: Double, _endX: Double, _endY: Double): Unit = {
    var line = js.Dynamic.literal().asInstanceOf[JsLine]
    line.x = _x
    line.y = _y
    line.endX = _endX
    line.endY = _endY
    lines.append(line)
  }
  
  def doesLineExist(_x: Double, _y: Double, _endX: Double, _endY: Double): Boolean = {
    lines.exists(l => {
      l.x == _x && l.y == _y && l.endX == _endX && l.endY == _endY
    })
  }
  
  def addRectWS(rect: JsRectangle): Unit = {
    val rectObj: Shape = Rectangle(rect.x, rect.y, rect.halfWidth, rect.halfHeight)
    val rectMsg: Message = AddShapeMessage(rectObj)
    sendOverWS(rectMsg)
  }
  
  def addCircleWS(circle: JsCircle): Unit = {
    val circObj: Shape = Circle(circle.x, circle.y, circle.radius)
    val circMsg: Message = AddShapeMessage(circObj)
    sendOverWS(circMsg)
  }
  
  def addLineWS(line: JsLine): Unit = {
    val lineObj: Shape = Line(line.x, line.y, line.endX, line.endY)
    val lineMsg: Message = AddShapeMessage(lineObj)
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
                    addRect(r.x, r.y, r.halfWidth, r.halfHeight)
                  }
                  case c: Circle => {
                    addCircle(c.x, c.y, c.radius)
                  }
                  case l: Line => {
                    addLine(l.x, l.y, l.endX, l.endY)
                  }
                }
              }
              case s: SyncCanvasMessage => {
                val shapes = s.canvas.shapes
                shapes.foreach(s => {
                  s match {
                    case r: Rectangle => {
                      val exists = doesRectExist(r.x, r.y, r.halfWidth, r.halfHeight)
                      if(!exists) {
                        addRect(r.x, r.y, r.halfWidth, r.halfHeight)
                      }
                    }
                    case c: Circle => {
                      val exists = doesCircleExist(c.x, c.y, c.radius)
                      if(!exists) {
                        addCircle(c.x, c.y, c.radius)
                      }
                    }
                    case l: Line => {
                      val exists = doesLineExist(l.x, l.y, l.endX, l.endY)
                      if(!exists) {
                        addLine(l.x, l.y, l.endX, l.endY)
                      }
                    }
                  }
                })
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