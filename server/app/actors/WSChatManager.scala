package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

class WSChatManager extends Actor {
  import WSChatManager._
  
  private var chatters = List[ActorRef]()
  
  def receive = {
    case NewChatter(chatter) =>
      chatters ::= chatter
    case BroadcastMessage(msg) =>
      chatters.foreach(_ ! WSChatActor.ChatMessage(msg))
  }
}

object WSChatManager {
  def props = Props[WSChatManager]
  
  case class NewChatter(chatter: ActorRef)
  case class BroadcastMessage(msg: String)
}