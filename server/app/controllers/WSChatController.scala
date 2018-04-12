package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.Inject
import play.api.libs.streams.ActorFlow
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import play.api.mvc.WebSocket
import actors.WSChatActor
import actors.WSChatManager

class WSChatController @Inject() (cc: ControllerComponents) (implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {
  val wsManager = system.actorOf(WSChatManager.props)
  
  def index = Action { implicit request =>
    Ok(views.html.wsChat())
  }
  
  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      WSChatActor.props(out, wsManager)
    }
  }
}