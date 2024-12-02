package part2actors

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.ActorLogging
import akka.actor.Props
import akka.event.Logging

object ActorLogging extends App {

  class SimpleActorWithExplicitLogger extends Actor {
    //  #1 - Explicit Logging
    val logger = Logging(context.system, this)

    /*
     * 1 - DEBUG
     * 2 - INFO
     * 3 - WARNING/WARN
     * 4 - ERROR
     * */
    override def receive: Receive = { case message =>
      logger.info(message.toString) // LOG it
    }
  }

  val system = ActorSystem("LoggingDemo")
  val actor =
    system.actorOf(Props[SimpleActorWithExplicitLogger], "simpleActor")

  actor ! "Logging a simple message"

  // #2 - Actor Logging
  class ActorWithLogging extends Actor with akka.actor.ActorLogging {
    override def receive: Receive = {
      case (a, b) =>
        log.info("Two things: {} and {}", a, b)
      case message =>
        log.info(message.toString) // LOG it
    }
  }

  val simpleActor =
    system.actorOf(Props[ActorWithLogging], "simpleActorWithLogging")
  simpleActor ! "Logging a simple message with an Actor with logging"

  simpleActor ! (42, 65)
}
