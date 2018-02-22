package chat

import akka.{Done, NotUsed}
import akka.remote.WireFormats.TimeUnit
import akka.stream.{Attributes, ClosedShape, Materializer}
import akka.stream.scaladsl._
import com.example.hello.api.{GreetingMessage, HelloService}
import com.lightbend.lagom.scaladsl.api.broker.Subscriber
import play.engineio.EngineIOController
import play.socketio.SocketIOSession
import play.socketio.scaladsl.SocketIO

import scala.collection.concurrent.TrieMap
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

  private val chatRooms = TrieMap.empty[String, Source[GreetingMessage, _]]

  // This gets an existing chat room, or creates it if it doesn't exist
  private def getChatRoom(user: String, room: String) = {
//    val (sink, source) = chatRooms.getOrElseUpdate(room, {
//
//    })



    val xxx = MergeHub.source[String]

//    val y = BroadcastHub.sink[String]
//
//    val x = helloService.greetingsTopic().subscribe.atMostOnceSource
//    val xx = x.map(y => (y, ))
//    val r = PartitionHub.statefulSink[GreetingMessage](() => (info, item) => item, 0)
//    val res = x.toMat(r)(Keep.right).run()
  }



  def roundRobin(): (PartitionHub.ConsumerInfo, GreetingMessage) ⇒ Long = {
    var i = -1L

    (info, elem) ⇒ {
    println(info.consumerIds)
    println("------------")
    println(elem)
    println("************")
      i += 1
      info.consumerIdByIdx((i % info.size).toInt)
    }
  }

  def getFlow(sessionId:String) = {
//    val x = Flow[String].map(x => GreetingMessage(x, List.empty))

//    x


//    val part = PartitionHub.statefulSink[GreetingMessage](() => roundRobin(), 2)
    println(s"-------------\nSession id is: $sessionId \n-----------------")
    val in = MergeHub.source[String].toMat(Sink.foreach[String](msg => {
      println("&&&&&&&&&&&&&&&&")
      println(s"My session id is: $sessionId")
      helloService.useGreeting(sessionId).invoke(GreetingMessage(msg, List(sessionId)))
    }))(Keep.left).run()




    val bb = BroadcastHub.sink[GreetingMessage]//.named(sessionId).
    val out2 = helloService.greetingsTopic().subscribe.atMostOnceSource.toMat(bb)(Keep.right).run()
      .takeWhile(
        m => {
          println(s"My session id is: $sessionId")
          println(s"consuming message ${m.message} and it is ${m.sendTo.contains(sessionId)} ")
          m.sendTo.contains(sessionId)
        })
//    val out2 = helloService.greetingsTopic().subscribe.atMostOnceSource.toMat(bb)(Keep.right).run()


//    val out = out2.filter(l => {
//      println(l)
//      println(s"##########Session id is: $sessionId")
//      l.sendTo.contains(sessionId)
//    })//.toMat(BroadcastHub.sink)(Keep.right).run
//    chatRooms.getOrElseUpdate(sessionId, out2)

    Flow.fromSinkAndSource(in, out2)
  }

  val controller: EngineIOController = socketIO.builder
      .addNamespace(decoder, encoder){
        case (SocketIOSession(sessionId, out),"/chat") => {
//          println("new one!")
          println(chatRooms)
          getFlow(sessionId)
        }
      }
    .createController()

}

