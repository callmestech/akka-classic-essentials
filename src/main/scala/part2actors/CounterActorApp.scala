package part2actors

import akka.actor.{Actor, Props, ActorSystem}

object CounterActorApp extends App {
  import CounterActor._

  /** Exercises
    *   1. A counter actor
    *      - Increment
    *      - Decrement
    *      - Print
    */
  class CounterActor extends Actor {
    var state: Int = 0

    override def receive: Receive = {
      case CounterMessage.Incr =>
        println("[counter actor] Incrementing...")
        state += 1
      case CounterMessage.Decr =>
        println("[counter actor] Decrementing...")
        state -= 1
      case CounterMessage.Print =>
        println(s"[counter actor] State: $state")
    }
  }

  object CounterActor {
    sealed trait CounterMessage

    object CounterMessage {
      case object Incr extends CounterMessage
      case object Decr extends CounterMessage
      case object Print extends CounterMessage
    }
  }

  val system = ActorSystem("CounterActorDemo")
  val counterActor = system.actorOf(Props[CounterActor])

  counterActor ! CounterMessage.Incr
  counterActor ! CounterMessage.Incr
  counterActor ! CounterMessage.Incr
  counterActor ! CounterMessage.Incr
  counterActor ! CounterMessage.Incr

  counterActor ! CounterMessage.Decr
  counterActor ! CounterMessage.Decr
  counterActor ! CounterMessage.Decr

  counterActor ! CounterMessage.Print
}
