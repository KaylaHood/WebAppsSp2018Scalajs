package sharedModels

import play.api.libs.json.JsValue
import play.api.libs.json.Writes
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.JsPath
import play.api.libs.json.Reads._
import play.api.libs.json.Format
import play.api.libs.functional.syntax._

sealed trait MessageCompanion {
  type T <: Any
  val id: Int
  implicit val reader: Reads[T]
  implicit val writer: Writes[T]
  implicit val format: Format[T] = Format(reader, writer)
}

/**
 * abstract class that the various message formats are built from
 */
abstract class Message {
  def companion: MessageCompanion = Message
  def getId(): Int = companion.id
}

object Message extends MessageCompanion {
  override type T = Message
  override val id: Int = -1
  override implicit val reader: Reads[Message] = AddShapeMessage.msgReader or SyncCanvasMessage.msgReader or InitSelfMessage.msgReader or NullMessage.msgReader
  override implicit val writer: Writes[Message] = new Writes[Message] {
    def writes(msg: Message) = msg match {
      case a: AddShapeMessage => AddShapeMessage.writer.writes(a)
      case s: SyncCanvasMessage => SyncCanvasMessage.writer.writes(s)
      case i: InitSelfMessage => InitSelfMessage.writer.writes(i)
      case n: NullMessage => NullMessage.writer.writes(n)
    }
  }
  override implicit val format: Format[Message] = Format(reader, writer)
}

class AddShapeMessage(val shape: Shape) extends Message {
  override def companion = AddShapeMessage
}

object AddShapeMessage extends MessageCompanion {
  override type T = AddShapeMessage
  override val id: Int = 0
  
  val msgReader: Reads[Message] = (
      (JsPath \ "shape").read[Shape]).map(AddShapeMessage(_))
  
  override val reader: Reads[AddShapeMessage] = (
      (JsPath \ "shape").read[Shape]).map(AddShapeMessage(_))
  
  override val writer: Writes[AddShapeMessage] = new Writes[AddShapeMessage] {
    def writes(msg: AddShapeMessage) = Json.obj(
        "shape" -> msg.shape)
  }
  
  override implicit val format: Format[AddShapeMessage] = Format(reader, writer)
  
  def apply(shape: Shape): AddShapeMessage = {
    new AddShapeMessage(shape)
  }
}

class RemoveShapesWithUserIdMessage(val userId: Int) extends Message {
  override def companion = RemoveShapesWithUserIdMessage
}

object RemoveShapesWithUserIdMessage extends MessageCompanion {
  override type T = RemoveShapesWithUserIdMessage
  override val id: Int = 1
  
  val msgReader: Reads[Message] = (
      (JsPath \ "removeUserId").read[Int]).map(RemoveShapesWithUserIdMessage(_))
  
  override val reader: Reads[RemoveShapesWithUserIdMessage] = (
      (JsPath \ "removeUserId").read[Int]).map(RemoveShapesWithUserIdMessage(_))
  
  override val writer: Writes[RemoveShapesWithUserIdMessage] = new Writes[RemoveShapesWithUserIdMessage] {
    def writes(msg: RemoveShapesWithUserIdMessage) = Json.obj(
        "removeUserId" -> msg.userId)
  }
  
  override implicit val format: Format[RemoveShapesWithUserIdMessage] = Format(reader, writer)
  
  def apply(userId: Int): RemoveShapesWithUserIdMessage = {
    new RemoveShapesWithUserIdMessage(userId)
  }
}

class SyncCanvasMessage(val canvas: Canvas) extends Message {
  override def companion = SyncCanvasMessage
}

object SyncCanvasMessage extends MessageCompanion {
  override type T = SyncCanvasMessage
  override val id: Int = 2
  
  val msgReader: Reads[Message] = (
      (JsPath \ "canvas").read[Canvas]).map(SyncCanvasMessage(_))
  
  override val reader: Reads[SyncCanvasMessage] = (
      (JsPath \ "canvas").read[Canvas]).map(SyncCanvasMessage(_))
  
  override val writer: Writes[SyncCanvasMessage] = new Writes[SyncCanvasMessage] {
    def writes(msg: SyncCanvasMessage) = Json.obj(
        "canvas" -> msg.canvas)
  }
  
  override implicit val format: Format[SyncCanvasMessage] = Format(reader, writer)
  
  def apply(canvas: Canvas): SyncCanvasMessage = {
    new SyncCanvasMessage(canvas)
  }
}

class InitSelfMessage(val selfId: Int) extends Message {
  override def companion = InitSelfMessage
}

object InitSelfMessage extends MessageCompanion {
  override type T = InitSelfMessage
  override val id: Int = 3
  
  val msgReader: Reads[Message] = (
      (JsPath \ "selfId").read[Int]).map(InitSelfMessage(_))
  
  override val reader: Reads[InitSelfMessage] = (
      (JsPath \ "selfId").read[Int]).map(InitSelfMessage(_))
  
  override val writer: Writes[InitSelfMessage] = new Writes[InitSelfMessage] {
    def writes(msg: InitSelfMessage) = Json.obj(
        "selfId" -> msg.selfId)
  }
  
  override implicit val format: Format[InitSelfMessage] = Format(reader, writer)
  
  def apply(selfId: Int): InitSelfMessage = {
    new InitSelfMessage(selfId)
  }
}

class NullMessage(val originId: Option[Int]) extends Message {
  override def companion = NullMessage
}

object NullMessage extends MessageCompanion {
  override type T = NullMessage
  override val id: Int = 4
  
  val msgReader: Reads[Message] = (
      (JsPath \ "originId").readNullable[Int]).map(NullMessage(_))
  
  override val reader: Reads[NullMessage] = (
      (JsPath \ "originId").readNullable[Int]).map(NullMessage(_))
  
  override val writer: Writes[NullMessage] = new Writes[NullMessage] {
    def writes(msg: NullMessage) = Json.obj(
        "originId" -> msg.originId)
  }
  
  override implicit val format: Format[NullMessage] = Format(reader, writer)
  
  def apply(originId: Option[Int]): NullMessage = {
    new NullMessage(originId)
  }
}