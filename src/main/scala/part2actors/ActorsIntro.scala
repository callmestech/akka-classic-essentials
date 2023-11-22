package part2actors

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props

object ActorsIntro extends App {

  // part1 - actor systems
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // part2 - create actors

  // Actors are uniquely identified
  // Messages are asynchronous
  // Each actor may respond differently
  // Actors are encapsulated
  class WorkCountActor extends Actor {
    var totalWords = 0

    // behaviour
    override def receive: Receive = {
      case message: String =>
        println(s"[word counter] I have received: $message")
        totalWords += message.split(" ").size
      case msg =>
        println(s"[word counter] I cannot understand ${msg.toString()}")
    }
  }

  // part 3 - instantiate our actor
  val wordCounter =
    actorSystem.actorOf(Props[WorkCountActor], "wordCounter")

  val anotherWordCounter =
    actorSystem.actorOf(Props[WorkCountActor], "anotherWordCounter")

  // part 4 - communicate !
  wordCounter ! "I am learning Akka and it's pretty damn cool!"
  anotherWordCounter ! "A different message"
  // asynchronous

  class Person(name: String) extends Actor {
    override def receive: Receive = { case "hi" =>
      println(s"[person actor] Hi, my name is $name")
    }
  }

  object Person {
    def props(name: String) =
      Props(new Person(name))
  }

  val person = actorSystem.actorOf(Person.props("Bob"))
  person ! "hi"
}
