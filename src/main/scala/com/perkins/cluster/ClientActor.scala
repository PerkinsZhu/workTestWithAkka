package com.perkins.cluster

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory

/**
  * Created by PerkinsZhu on 2018/12/29 17:16
  **/
class ClientActor extends Actor with ActorLogging {
  override def preStart(): Unit = {
    log.info("pre start ClientActor")

    // 直接构造serverActor
    val server = context.actorOf(Props[ServerActor], name = "serverActor")
    log.info("server==================>" + server.path)
    server ! Message("client-01", "i am client-01")

    // 使用路由构造 server
    val serverProxy = context.actorOf(FromConfig.props(), name = "serverRouter")

    log.info("serverProxy-props==================>" + FromConfig.props())
    log.info("serverProxy==================>" + serverProxy.path)
    serverProxy ! Message("client-01-serverRoute", "i am client-01")
  }

  override def receive: Receive = {
    case msg: Message => {
      log.info(s"receive message from ${msg.name},conten:${msg.msg}")
    }
  }
}

object ClientActor {
  def startTask(): Unit = {
    val clientConfig = ConfigFactory.parseString("akka.cluster.roles = [frontend]").
      withFallback(ConfigFactory.load("factorial"))
    val system = ActorSystem("PerkinsCluster", clientConfig)
//    system.actorOf(Props[ClientActor], name = "clientActor")

    Cluster(system) registerOnMemberUp {
      // 注意,这里的名字用在配置文件factorial.conf 的负载均衡配置路径中
      system.actorOf(Props(classOf[ClientActor]), name = "clientActor")
    }
  }
}