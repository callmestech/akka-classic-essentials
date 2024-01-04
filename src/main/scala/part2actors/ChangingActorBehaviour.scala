package part2actors

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef

object ChangingActorBehaviour extends App {

  class FussyKid extends Actor {
    import FussyKid._
    import Mom._

    var state = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLE) =>
        state = SAD
      case Food(CHOCOLATE) =>
        state = HAPPY
      case Ask(_) =>
        if (state == HAPPY)
          sender() ! KidAccept
        else
          sender() ! KidReject
    }
  }

  object FussyKid {
    val HAPPY = "happy"
    val SAD = "sad"

    case object KidAccept
    case object KidReject
  }

  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive =
      happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) =>
        context.become(sadReceive, false)
      case Food(CHOCOLATE) =>
        ()
      case Ask(_) =>
        sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) =>
        context.become(sadReceive, false)
      case Food(CHOCOLATE) =>
        context.unbecome()
      case Ask(_) =>
        sender() ! KidReject
    }
  }

  class Mom extends Actor {
    import Mom._
    import FussyKid._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("Do you want to play?")
      case KidAccept =>
        println("Yay, my kid is happy")
      case KidReject =>
        println("My kid is sad, but as he's healthy!")
    }
  }

  object Mom {
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"

    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
  }

  val system = ActorSystem("ChangingActorBehaviourDemo")

  val fussyKid = system.actorOf(Props[FussyKid])
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])
  val mom = system.actorOf(Props[Mom])

  mom ! Mom.MomStart(statelessFussyKid)
}
