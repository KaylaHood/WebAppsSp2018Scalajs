package controllers

import play.api.libs.json._
import play.api.mvc._
import play.api.libs.streams.ActorFlow
import javax.inject._
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.mvc.WebSocket.MessageFlowTransformer
import akka.actor._
import actors.WSActor
import actors.WSManager
import models._

/**
 * "WSController" = "Web Socket Controller"
 * handles requests for web socket connections
 */
@Singleton
class WSController @Inject() (cc:ControllerComponents) (implicit system: ActorSystem, mat: Materializer, assetsFinder: AssetsFinder) extends AbstractController(cc) {
  val wsManager = system.actorOf(WSManager.props)
  
  implicit val msgFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[Message, Message]
  
  def socket = WebSocket.accept[Message, Message] { request =>
    ActorFlow.actorRef { out =>
      WSActor.props(out, wsManager)
    }
  }
  
}