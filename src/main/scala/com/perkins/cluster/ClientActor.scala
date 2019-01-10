package com.perkins.cluster

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

/**
  * Created by PerkinsZhu on 2018/12/29 17:16
  **/
class ClientActor extends Actor with ActorLogging {
  override def preStart(): Unit = {
    import context.dispatcher
    log.info("pre start ClientActor")

    // 直接构造serverActor
    val server = context.actorOf(Props[ServerActor], name = "serverActor")
    log.info("server==================>" + server.path)
    server ! Message("client-01", "i am client-01")

    // 使用路由构造 server,这里会实现负载均衡，自动选择server处理tell请求
    val serverProxy = context.actorOf(FromConfig.props(), name = "serverRouter")
    log.info("serverProxy==================>" + serverProxy.path)

    Future {
      while (true) {
        serverProxy ! Message("client-01-serverRoute", "message--->"+new Date().getTime) //使用 serverProxy会实现负载均衡，因为在配置文件中有配置
//        server ! Message("client-01-serverRoute", "i am client-01")  //使用 server不会实现负载均衡
        /**
          * 注意两个server的路径
          *   server: akka://PerkinsCluster@127.0.0.1:62889/user/clientActor/serverActor
          *   serverProxy: akka://PerkinsCluster@127.0.0.1:2552/user/serverActor
          *   这两个路径是不一样的。
          *   server是从clientActor中创建的
          *   serverProxy是在配置文件中配制的
          */

        Thread.sleep(100)
      }
    }
  }

  override def receive: Receive = {
    case msg: Message => {
      log.info(s"receive message from ${msg.name},conten:${msg.msg}")
    }
    case data:Set[String] =>{
      println("接收到查询结果"+data)
    }
    case data =>{
      println("接收到未知消息"+data)
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
      println("---------registerOnMemberUpp---")
      // 注意,这里的名字用在配置文件factorial.conf 的负载均衡配置路径中
      val actor = system.actorOf(Props(classOf[ClientActor]), name = "clientActor")
      println("registerOnMemberUp===>" + actor.path)
    }
  }
}