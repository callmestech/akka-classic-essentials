package part2actors

import akka.actor.{Actor, ActorSystem, ActorRef, Props}
import scala.collection.mutable.ArrayBuffer

object BankActorApp extends App {
  import BankAccountActor._

  // 2. A Bank account as an actor
  // receives:
  // - Deposit an amount
  // - Withdraw an amount
  // - Statement
  // replies with
  // - Success
  // - Failure
  //
  // interact with other actor
  class BankAccountActor extends Actor {

    var amount: Double = 0.0
    var lastOperationId: Int = 0
    val operations: ArrayBuffer[HistoryRecord] =
      ArrayBuffer.empty

    def newId(): Int = {
      lastOperationId += 1
      lastOperationId
    }

    def mkStatement: String =
      s"${Console.BLUE}${operations.map(_.toString()).mkString("\n")}\nBalance: $amount${Console.RESET}"

    override def receive: Receive = {
      case BankAccountMessage.Statement(user) =>
        val id = newId()
        operations.append(
          HistoryRecord(
            operationId = id,
            operationType = "Statement",
            amount = None,
            succeed = true
          )
        )
        user ! BankAccountResponse.Success(
          operationId = id,
          operationType = "Statement",
          message = Some(mkStatement)
        )
      case BankAccountMessage.Withdraw(withdraw, user) =>
        val id = newId()
        val historyTemplate = HistoryRecord(
          operationId = id,
          operationType = "Withdraw",
          amount = Some(withdraw),
          succeed = true
        )

        val (historyRecord, msg) =
          if (withdraw.toDouble > amount) {
            historyTemplate
              .copy(succeed = false) -> BankAccountResponse.Failure(
              operationId = id,
              operationType = "Withdraw",
              message = Some("Not enough money!")
            )
          } else {
            amount -= withdraw.toDouble
            historyTemplate -> BankAccountResponse.Success(
              operationId = id,
              operationType = "Withdraw",
              message = None
            )
          }
        operations.append(historyRecord)
        user ! msg

      case BankAccountMessage.Deposit(deposit, user) =>
        val id = newId()
        val historyTemplate = HistoryRecord(
          operationId = id,
          operationType = "Deposit",
          amount = Some(deposit),
          succeed = true
        )
        val (historyRecord, msg) =
          if (deposit > 0) {
            amount += deposit.toDouble
            historyTemplate -> BankAccountResponse.Success(
              operationId = id,
              operationType = "Deposit",
              message = None
            )
          } else {
            historyTemplate
              .copy(succeed = false) -> BankAccountResponse.Failure(
              operationId = id,
              operationType = "Deposit",
              message = Some(s"Invalid deposit amount $deposit")
            )
          }

        operations.append(historyRecord)
        user ! msg
    }
  }

  object BankAccountActor {
    sealed trait BankAccountMessage {
      def user: ActorRef
    }

    object BankAccountMessage {
      final case class Deposit(amount: Int, user: ActorRef)
          extends BankAccountMessage
      final case class Withdraw(amount: Int, user: ActorRef)
          extends BankAccountMessage
      final case class Statement(user: ActorRef) extends BankAccountMessage
    }

    sealed trait BankAccountResponse {
      def operationId: Int
      def operationType: String
      def message: Option[String]
      def isSuccess: Boolean
    }

    final case class HistoryRecord(
        operationId: Int,
        operationType: String,
        amount: Option[Int],
        succeed: Boolean
    ) {
      override def toString(): String =
        s"[operation $operationId] $operationType ${amount.getOrElse("")} successfull: $succeed"
    }

    object BankAccountResponse {
      final case class Success(
          operationId: Int,
          operationType: String,
          message: Option[String]
      ) extends BankAccountResponse {
        def isSuccess: Boolean = true
      }
      final case class Failure(
          operationId: Int,
          operationType: String,
          message: Option[String]
      ) extends BankAccountResponse {
        def isSuccess: Boolean = false
      }
    }

  }

  class BankUserActor extends Actor {
    override def receive: Receive = { case res: BankAccountResponse =>
      if (res.isSuccess) {
        println(
          s"[user] operation ${res.operationId} ${res.operationType} succeeded.\n${res.message.getOrElse("")}"
        )
      } else {
        println(
          s"[user] operation ${res.operationId} ${res.operationType} failed.\n${res.message.getOrElse("")}"
        )
      }
    }
  }

  val system = ActorSystem("BankActorApp")
  val user = system.actorOf(Props[BankUserActor])
  val account = system.actorOf(Props[BankAccountActor])

  account ! BankAccountMessage.Statement(user)
  account ! BankAccountMessage.Deposit(100, user)
  account ! BankAccountMessage.Statement(user)
  account ! BankAccountMessage.Withdraw(50, user)
  account ! BankAccountMessage.Statement(user)
  account ! BankAccountMessage.Withdraw(60, user)
  account ! BankAccountMessage.Statement(user)

}
