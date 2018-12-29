package com.perkins.cluster

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by PerkinsZhu on 2018/12/29 17:15
  **/
object TestApp {
  def main(args: Array[String]): Unit = {

    val serverConfig = ConfigFactory.parseString(s"""
        akka.remote.artery.canonical.port=2551
        """)
      .withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]"))
      .withFallback(ConfigFactory.load("factorial"))
    val serverSystem = ActorSystem("ClusterSystem", serverConfig)
    serverSystem.actorOf(Props[ServerActor], name = "factorialBackend")


    val clientConfig = ConfigFactory.parseString("akka.cluster.roles = [frontend]").
      withFallback(ConfigFactory.load("factorial"))
    val clientSystem = ActorSystem("PerkinsCluster",clientConfig)
    clientSystem.actorOf(Props[ClientActor], name = "clientActor")




  }


}
