package part2actors

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import scala.collection.mutable.ArrayBuffer

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" =>
        // replying with a message
        context.sender() ! "Hello, there!"
      case message: String =>
        println(s"[${self}] I have received $message from ${context.sender()}")
      case number: Int =>
        println(s"[${self}] I have received a number: $number")
      case SpecialMessage(contents) =>
        println(
          s"[${self}] I have received something special: $contents"
        )
      case SendMessageToYourself(content) =>
        self ! content
      case SayHiTo(ref) =>
        ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) =>
        ref forward (
          content
            .toCharArray()
            .map(c => (c + 2).toChar)
            .mkString
        ) // I keep the original sender of the message
    }
  }

  final case class SpecialMessage(contents: String)
  final case class SendMessageToYourself(content: String)
  final case class SayHiTo(ref: ActorRef)
  final case class WirelessPhoneMessage(content: String, ref: ActorRef)

  val system = ActorSystem("ActorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "hello, actor!"

  // 1 - messages can be of any type
  // a) messages must be IMMUTABLE b) messages must be SERIALIZABLE
  // in practice use case classes and case objects

  // 2 - actors have information about their context and anout themselves
  simpleActor ! 32
  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it")

  // 3 - actors can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  alice ! SayHiTo(bob)

  // 4 - dead letters
  alice ! "Hi!"

  // 5 - forwarding messages
  alice ! WirelessPhoneMessage("Jeppa", bob)

}
