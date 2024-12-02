package part2actors

import akka.actor.AbstractActor.Receive
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = { case message =>
      log.info(s"[${self.path}] $message")
    }
  }

  /** 1 - Inline configuration
    */
  val configString =
    """
      | akka {
      |   loglevel = "DEBUG"
      | }
      |""".stripMargin

  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ConfigurationDemo", config)
  val actor = system.actorOf(Props[SimpleLoggingActor])

  actor ! "A message to remember"

  /** 2 - Configuration file
    */

  val defaultConfigSystem = ActorSystem("DefaultConfigFileDemo")
  val defaultConfigActor =
    defaultConfigSystem.actorOf(Props[SimpleLoggingActor])

  defaultConfigActor ! "Remember me!"

  /** 3 - Separate configuration in the same file
    */
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("SpecialConfigDemo", specialConfig)
  val specialConfigActor =
    specialConfigSystem.actorOf(Props[SimpleLoggingActor])

  specialConfigActor ! "Remember me, I'm special!"

  /** 4 - Separate configuration in another file
    */
  val separateConfig =
    ConfigFactory.load(
      "secret_folder/secret_configuration.conf"
    )
  println(
    s"Separate config log level: ${separateConfig.getString("akka.logLevel")}"
  )

  /** 5 - Different file formats JSON, Properties
    */
  val jsonConfig = ConfigFactory.load("json/config.json")
  println(s"json config: ${jsonConfig.getString("aJsonProperty")}")
  println(s"json config: ${jsonConfig.getString("akka.loglevel")}")

  val propsConfig = ConfigFactory.load("props/config.properties")
  println(s"properties config: ${propsConfig.getString("my.config.key")}")
  println(s"properties config: ${propsConfig.getString("akka.loglevel")}")
}
