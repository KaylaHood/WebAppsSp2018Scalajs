package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

class WSChatActor(out: ActorRef, manager: ActorRef) extends Actor {
  
  out ! "We are connected!"
  manager ! WSChatManager.NewChatter(self)
  
  import WSChatActor._
  
  def receive = {
    case input: String =>
      manager ! WSChatManager.BroadcastMessage(input)
    case ChatMessage(msg) => 
      out ! msg
    case m => 
      println("Got unknown message: "+m)
  }
}

object WSChatActor {
  def props(out: ActorRef, manager: ActorRef) = Props(new WSChatActor(out, manager))
  
  case class ChatMessage(msg: String)
}