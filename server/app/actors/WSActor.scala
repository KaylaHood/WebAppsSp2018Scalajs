package actors

import play.api.libs.json._
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import sharedModels._

/**
 * "WSActor" = "Web Socket Actor"
 * represents a client's connection to the server via a web socket
 */
class WSActor(val out: ActorRef, val manager: ActorRef) extends Actor {
  import WSActor._
  import WSManager._
  
  private var id: Int = -1 //  temporary value (set in preStart())
  
  override def preStart() {
    this.id = WSManager.getNextClientID
    //println("A new client has connected to a web socket. WSActor ID: " + this.id)
    // tell manager that this actor has been connected
    this.manager.tell(WSManager.NewActor(this), self)
    // send initialization message to client
    val message = new InitSelfMessage(this.id)
    out.tell(message, self)
  }
  
  def receive = {
    case msgIn: Message => {
      msgIn match {
        case a: AddShapeMessage => {
          //println("WSActor with id: " + this.id + " received an AddShapeMessage")
          manager.tell(AddShape(a), self)
        }
        case s: SyncCanvasMessage => {
          //println("WSActor with id: " + this.id + " received a SyncCanvasMessage")
          //println("WSActors are not supposed to receive SyncCanvasMessages from the client")
        }
        case u: RemoveShapesWithUserIdMessage => {
          //println("WSActor with id: " + this.id + " received a RemoveShapesWithUserIdMessage")
          //println("WSActors are not supposed to receive RemoveShapesWithUserIdMessages from the client")
        }
        case i: InitSelfMessage => {
          //println("WSActor with id: " + this.id + " received an InitSelfMessage")
          //println("WSActors are not supposed to receive InitSelfMessages from the client")
        }
        case n: NullMessage => {
          //println("WSActor with id: " + this.id + " received a NullMessage")
        }
      }
    }
    case msgOut: MessageOut => {
      //println("WSActor with id: " + this.id + " was told to send a message to its client.")
      out.tell(msgOut.msg, self)
    }
    case m => {
      //println("WSActor with id: " + this.id + " recieved an unknown message: " + m.toString())
    }
  }
  
  override def postStop() {
    //println("WSActor with id: " + id + " is disconnecting...")
    manager.tell(WSManager.RemoveActor(this), self)
  }
}

object WSActor {
  def props(out: ActorRef, manager: ActorRef) = Props(new WSActor(out, manager))
  
  // Any message that the actor should send back to the client is stored in a MessageOut.
  // The WSManager sends MessageOut instances to the WSActors it manages.
  case class MessageOut(val msg: Message)

  class WSActorRef(val ref: ActorRef, val id: Int) {
    def tell(msg: Any, sender: ActorRef) = {
      ref.tell(msg, sender)
    }
  }
  
  implicit def getWSActorRef(actor: WSActor): WSActorRef = {
    new WSActorRef(actor.self, actor.id)
  }
}