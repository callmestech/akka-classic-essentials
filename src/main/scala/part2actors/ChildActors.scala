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

  // Guardian actors (top-level)
  // - /system = system Guardian
  // - /user = user-level guardian
  // - / = the root guardian

  /** Actor selection
    */
  val childSelection = system.actorSelection("/user/parent/child2")
  childSelection ! "I found you"

  /** Danger!
    *
    * NEVER PASS MUTABLE ACTOR STATE, OR THE `THIS` REFERENCE, TO CHILD ACTORS.
    *
    * NEVER IN YOUR LIFE.
    */

  object NaiveBankAccount {
    final case class Deposit(amount: Int)
    final case class Withdraw(amount: Int)
    case object InitializeAccount
  }

  class NaiveBankAccount extends Actor {
    import NaiveBankAccount._
    import CreditCard._

    var amount = 0

    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard], "card")
        creditCardRef ! AttachToAccount(this) // !!
      case Deposit(funds) =>
        deposit(funds)
      case Withdraw(funds) =>
        withdraw(funds)
    }

    def deposit(funds: Int) = {
      println(s"${self.path} depositing $funds on top of $amount")
      amount += funds
    }

    def withdraw(funds: Int) = {
      println(s"${self.path} withdrowing $funds from $amount")
      amount -= funds
    }
  }

  object CreditCard {
    final case class AttachToAccount(bankAccount: NaiveBankAccount) // !!
    case object CheckStatus
  }

  class CreditCard extends Actor {
    import CreditCard._

    override def receive: Receive = { case AttachToAccount(account) =>
      context.become(attachedTo(account))
    }

    def attachedTo(account: NaiveBankAccount): Receive = { case CheckStatus =>
      println(s"${self.path} your message has been processed.")
      account.withdraw(1) // because I can
    }
  }

  import NaiveBankAccount._
  import CreditCard._

  val bankAccountRef = system.actorOf(Props[NaiveBankAccount], "account")
  bankAccountRef ! InitializeAccount
  bankAccountRef ! Deposit(100)

  Thread.sleep(500)
  val ccSelection = system.actorSelection("/user/account/card")
  ccSelection ! CheckStatus

  // WRONG !!!!
}
