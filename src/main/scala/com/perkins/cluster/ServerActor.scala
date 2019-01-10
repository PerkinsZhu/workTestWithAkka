package com.perkins.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ddata._
import akka.cluster.ddata.Replicator._
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

/**
  * Created by PerkinsZhu on 2018/12/29 17:16
  **/
class ServerActor extends Actor with ActorLogging {
  val Counter1Key = PNCounterKey("counter1")
  val Set1Key = GSetKey[String]("set1")
  val Set2Key = ORSetKey[String]("set2")
  val ActiveFlagKey = FlagKey("active")

  implicit val cluster = Cluster(context.system)

  val replicator = DistributedData(context.system).replicator

  replicator ! Subscribe(Set1Key, self) //订阅数据的变化

  override def receive: Receive = {
    case msg: Message => {
      log.info(s"receive message from ${msg.name},conten:${msg.msg}")
      //      val actor= sender()
      //      actor ! Message(self.path.address.toString,"i hava message")

      // 分布式数据集
      replicator ! Get(Set1Key, ReadLocal, Some(sender()))
      //      replicator ! Update(Counter1Key, PNCounter(), WriteLocal)(_ + 1)

      val writeMajority = WriteMajority(timeout = 5.seconds)
      // 从数据中删除set1key
      // replicator ! Delete(Set1Key, writeMajority,Some(sender()))


      val writeTo3 = WriteTo(n = 2, timeout = 1.second)
      replicator ! Update(Set1Key, GSet.empty[String], writeTo3)(item => {
        println("---send--->" + item)
        item + msg.msg
      })

    }
    case UpdateSuccess(Counter1Key, Some(replyTo: ActorRef)) ⇒ {
      replyTo ! "ack"
    }
    case UpdateTimeout(Counter1Key, Some(replyTo: ActorRef)) ⇒ {
      replyTo ! "nack"
    }
    case g@GetSuccess(Set1Key, Some(replyTo: ActorRef)) ⇒ {
      val elements = g.get(Set1Key).elements
      log.info("getResult--->" + elements)
      replyTo ! elements
    }
    case GetFailure(Set1Key, req) ⇒
    // read from 3 nodes failed within 1.second
    case NotFound(Set1Key, req) ⇒ // key set1 does not exist
    case GetSuccess(key, data) => {
      log.info("GetSuccess--->" + key + "====>" + data)
    }
    case UpdateSuccess(key, data) => {
      log.info("UpdateSuccess--->" + key + "====>" + data)
    }
    case c@Changed(Set1Key) ⇒ {
      val currentValue = c.get(Set1Key)
      log.info("当前数据发生改变" + currentValue)
    }
    case c@Changed(key) ⇒ {
      val currentValue = c.get(key)
      log.info(s"当前数据【${key}】发生改变" + currentValue)
    }
    case c@DataDeleted(key, Some(request: ActorRef)) ⇒ {
      log.info(s"数据${key}被删除掉")
      request ! s"${key}已被删除"
    }
    case a: Any => log.info("unknowMessage-->" + a.toString)
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
