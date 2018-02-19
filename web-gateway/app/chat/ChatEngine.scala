package chat

import akka.{Done, NotUsed}
import akka.remote.WireFormats.TimeUnit
import akka.stream.{ClosedShape, Materializer}
import akka.stream.scaladsl._
import com.example.hello.api.{GreetingMessage, HelloService}
import play.engineio.EngineIOController
import play.socketio.scaladsl.SocketIO

import scala.concurrent.duration._

/**
  * A simple chat engine.
  */
class ChatEngine(socketIO: SocketIO, helloService: HelloService)(implicit mat: Materializer) {

  import play.socketio.scaladsl.SocketIOEventCodec._

  // This will decode String "chat message" events coming in
  val decoder = decodeByName {
    case "chat message" => decodeJson[String]
  }

  val encoder = encodeByType[GreetingMessage] {
    case _: GreetingMessage=> "chat message" -> encodeJson[GreetingMessage]
  }


  val in = Sink.foreach[String](msg => helloService.useGreeting("same").invoke(GreetingMessage(msg)))

  val out =
    helloService
      .greetingsTopic()
      .subscribe
      .atMostOnceSource

  val fl = Flow.fromSinkAndSourceCoupled(in, out)

  // Here we create an EngineIOController to handle requests for our chat
  // system, and we add the chat flow under the "/chat" namespace.
  val controller: EngineIOController = socketIO.builder
    .addNamespace("/chat", decoder, encoder, fl)
    .createController()


}

