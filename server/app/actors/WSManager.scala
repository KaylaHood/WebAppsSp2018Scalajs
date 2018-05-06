package actors

import play.api.libs.json._
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import WSActor._
import sharedModels._

/**
 * "WSManager" = "Web Socket Manager"
 * manages active web sockets connected to the server
 */
class WSManager extends Actor {
  import WSManager._
  private var clients = List[WSActorRef]()
  private var canvas = new Canvas()
  
  def receive = {
    case RemoveActor(actorToRemove) =>
      removeClient(actorToRemove)
    case NewActor(newActor) =>
      addClient(newActor)
    case AddShape(shapeMsg) =>
      addShape(shapeMsg)
    case BroadcastMessage(msg) =>
      //println("Broadcasting message \"" + msg + "\" to all clients...")
      // TODO: add particles to canvas, etc
      clients.foreach(c => c.tell(WSActor.MessageOut(msg), self))
  }
  
  def addShape(shapeMsg: AddShapeMessage) = {
    //println("WSManager is adding the shape with class " + shapeMsg.shape.getClass)
    canvas.addShape(shapeMsg.shape)
    clients.foreach(c => c.tell(WSActor.MessageOut(shapeMsg), self))
  }
  
  def addClient(clientRef: WSActorRef) = {
    //println("WSManager is adding the WSActor with id: " + clientRef.id)
    clients ::= clientRef
    val msgSyncCanvas = new SyncCanvasMessage(this.canvas)
    clientRef.tell(WSActor.MessageOut(msgSyncCanvas), self)
  }
  
  def removeClient(clientRef: WSActorRef) = {
    //println("WSManager is removing the WSActor with id: " + clientRef.id)
    clients = clients.filterNot(x => x.ref == clientRef.ref)
  }

}

object WSManager {
  private var nextClientID = 0
  
  def getNextClientID = {
    var id = nextClientID
    nextClientID += 1
    id
  }
  
  def props = Props[WSManager]
  
  case class RemoveActor(clientActor: WSActorRef)
  case class NewActor(clientActor: WSActorRef)
  case class AddShape(shapeMsg: AddShapeMessage)
  case class BroadcastMessage(msg: Message)
}