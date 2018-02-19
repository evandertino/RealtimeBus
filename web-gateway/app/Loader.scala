import chat.ChatEngine
import com.example.hello.api.HelloService
import com.lightbend.lagom.scaladsl.api.{LagomConfigComponent, ServiceAcl, ServiceInfo}
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaClientComponents
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.softwaremill.macwire._
import controllers.{Assets, AssetsComponents}
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, Mode}
import play.filters.HttpFiltersComponents
import play.i18n.I18nComponents
import play.socketio.scaladsl.SocketIOComponents
import router.Routes

import scala.collection.immutable
import scala.concurrent.ExecutionContext

abstract class WebGateway(context: Context) extends BuiltInComponentsFromContext(context)
  with AssetsComponents
  with HttpFiltersComponents
  with AhcWSComponents
  with SocketIOComponents
  with LagomConfigComponent
  with LagomKafkaClientComponents
  with LagomServiceClientComponents {

  override lazy val serviceInfo: ServiceInfo = ServiceInfo(
    "web-gateway",
    Map(
      "web-gateway" -> immutable.Seq(ServiceAcl.forPathRegex("(?!/api/).*"))
    )
  )
//  override implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher

  override lazy val router = {
    val prefix = "/"
    wire[Routes]
  }

  override lazy val httpFilters = Nil

  lazy val chatEngine :ChatEngine = wire[ChatEngine]
  lazy val chatEngineController = chatEngine.controller
  lazy val helloSerive = serviceClient.implement[HelloService]

}

class WebGatewayLoader extends ApplicationLoader {
  override def load(context: Context) =
  (new WebGateway(context) with LagomDevModeComponents).application
//  override def load(context: Context) = context.environment.mode match {
//    case Mode.Dev =>
//      (new WebGateway(context) with LagomDevModeComponents).application
//    case _ =>
//      (new WebGateway(context) with ConductRApplicationComponents).application
//  }
}