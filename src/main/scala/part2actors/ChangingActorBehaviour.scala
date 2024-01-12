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

  // Exercise 1
  // 1 - recreate the Counter actor with context.become and no Mutable state
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._

    override def receive: Receive =
      countReceive(0)

    def countReceive(currentCount: Int): Receive = {
      case Increment =>
        context.become(countReceive(currentCount + 1))
      case Decrement =>
        context.become(countReceive(currentCount - 1))
      case Print =>
        println(s"[counter actor] The count is $currentCount")
    }
  }

  // Exercise 2
  // A simplified voting system
  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {

    override def receive: Receive =
      emptyVote

    def emptyVote: Receive = {
      case Vote(name) =>
        context.become(nonEmptyVote(name))
      case VoteStatusRequest =>
        context.sender() ! VoteStatusReply(None)
    }

    def nonEmptyVote(candidate: String): Receive = {
      case Vote(name) =>
        context.become(nonEmptyVote(name))
      case VoteStatusRequest =>
        context.sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])

  class VoteAggregator extends Actor {

    override def receive: Receive =
      impl(Set.empty, Map.empty)

    def impl(awaiting: Set[ActorRef], state: Map[String, Int]): Receive = {
      case AggregateVotes(citizens) =>
        context.become(impl(citizens, state))
        citizens.foreach(_ ! VoteStatusRequest)
      case VoteStatusReply(Some(candidate)) =>
        val newAwaiting = awaiting.excl(context.sender())
        val newState = state.updatedWith(candidate) {
          case Some(value) =>
            Some(value + 1)
          case _ =>
            Some(1)
        }
        context.become(impl(newAwaiting, newState))
        if (newAwaiting.isEmpty) {
          println(newState.map { case (k, v) => s"$k -> $v" }.mkString("\n"))
        }
      case VoteStatusReply(_) =>
        ()
    }
  }
  // 1
  val counter = system.actorOf(Props[Counter])
  counter ! Counter.Increment
  counter ! Counter.Increment
  counter ! Counter.Increment
  counter ! Counter.Increment
  counter ! Counter.Decrement
  counter ! Counter.Decrement
  counter ! Counter.Print

  // 2
  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

  /*
   * Print the status of the voting
   *
   * Martin -> 1
   * Jonas -> 1
   * Roland -> 2
   * */
}
