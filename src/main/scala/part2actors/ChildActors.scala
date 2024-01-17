package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActors extends App {

  class Parent extends Actor {
    import Parent._

    override def receive: Receive = { case CreateChild(name) =>
      println(s"${self.path} creating child")
      // create a new actor right HERE
      val childRef = context.actorOf(Props[Child], name)
      context.become(withChild(childRef))
    }

    def withChild(child: ActorRef): Receive = { case TellChild(message) =>
      child.forward(message)
    }
  }

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(name: String)
  }

  class Child extends Actor {
    override def receive: Receive = { case message =>
      println(s"${self.path} I got: $message")
    }
  }

  import Parent._
  val system = ActorSystem("ParentChildDemo")
  val parent = system.actorOf(Props[Parent], "parent")

  parent ! CreateChild("kiddo")
  parent ! TellChild("Hey kiddo!")
}
