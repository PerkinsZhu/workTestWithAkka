package com.perkins.cluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by PerkinsZhu on 2018/12/29 17:16
  **/
class ServerActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case msg: Message => {
      log.info(s"receive message from ${msg.name},conten:${msg.msg}")
    }
  }
}


object ServerActor {

  def startTask(port: Int): Unit = {
    val serverConfig = ConfigFactory.parseString(
      s"""
        akka.remote.artery.canonical.port=${port}
        """)
      .withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]"))
      .withFallback(ConfigFactory.load("factorial"))
    val system = ActorSystem("PerkinsCluster", serverConfig)
    system.actorOf(Props[ServerActor], name = "serverActor")
    system.actorOf(Props[MetricsListener], name = "metricsListener")
  }

}
