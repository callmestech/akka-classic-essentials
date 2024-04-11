package part2actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorSystem

object ChildActorsExercise extends App {

  // Distributed Word counting
  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    val queue = new scala.collection.mutable.Queue[ActorRef]

    override def receive: Receive = {
      case Initialize(nChildren) =>
        println(s"[wcm] Initializing $nChildren children")
        (1 to nChildren).foreach { i =>
          val child = context.actorOf(WordCounterWorker.props(s"wcw_$i"))
          queue.enqueue(child)
        }

      case msg @ WordCountTask(id, text) =>
        println(s"[wcm] I have received a task $id")

        if (queue.isEmpty) {
          println(s"[wcm] I don't have any workers to process the task")
          println(s"[wcm] I will sleep for 1 second and try again")
          Thread.sleep(1000)
          println(s"[wcm] I will try again")
          self ! msg
        } else {
          val worker = queue.dequeue()
          worker ! msg
        }
      case WordCountReply(taskId, result) =>
        println(s"[wcm] Result is $result")
        queue.enqueue(sender())
    }
  }

  object WordCounterMaster {
    final case class Initialize(nChildren: Int)
    final case class WordCountTask(taskId: Int, text: String)
    final case class WordCountReply(taskId: Int, count: Int)
  }

  class WordCounterWorker(id: String) extends Actor {
    import WordCounterMaster._

    override def receive: Receive = { case WordCountTask(taskId, text) =>
      println(s"[$id] I have received a task $taskId")
      sender() ! WordCountReply(taskId, text.split(" ").length)
    }
  }

  object WordCounterWorker {
    def props(id: String) = Props(new WordCounterWorker(id))
  }

  /*
   * create WordCounterMaster
   * send Initialize(10) to WordCounterMaster
   * send "Akka is awesome" to WordCounterMaster
   *    wcm will send a WordCountTask("Akka is awesome") to one of its children
   *       child replies with WordCountReply(3) to the master
   *    master replies with 3 to the sender
   *
   * requester -> wcm -> wcw
   *         r <- wcm <-
   *
   * round robin logic
   * 1, 2, 3, 4, 5 and 7 tasks
   * 1, 2, 3, 4, 5, 1, 2
   * */
  val system = ActorSystem("WordCounterExercise")
  val wcm = system.actorOf(Props[WordCounterMaster])

  wcm ! WordCounterMaster.Initialize(3)

  (1 to 10).foreach { i =>
    wcm ! WordCounterMaster.WordCountTask(i, "word " * i)
  }
}
