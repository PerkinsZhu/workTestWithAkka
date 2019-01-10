package com.perkins.distributeddata

import java.util.concurrent.ThreadLocalRandom
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.cluster.Cluster
import akka.cluster.ddata.DistributedData
import akka.cluster.ddata.ORSet
import akka.cluster.ddata.ORSetKey
import akka.cluster.ddata.Replicator
import akka.cluster.ddata.Replicator._
import scala.concurrent.duration._

object DataBot {

  private case object Tick

}

class DataBot extends Actor with ActorLogging {

  import DataBot._

  val replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)

  import context.dispatcher

  val tickTask = context.system.scheduler.schedule(5.seconds, 5.seconds, self, Tick)

  val DataKey = ORSetKey[String]("key")

  replicator ! Subscribe(DataKey, self)

  def receive = {
    case Tick ⇒
      val s = ThreadLocalRandom.current().nextInt(97, 123).toChar.toString
      if (ThreadLocalRandom.current().nextBoolean()) {
        // add
        log.info("Adding: {}", s)
        replicator ! Update(DataKey, ORSet.empty[String], WriteLocal)(_ + s)
      } else {
        // remove
        log.info("Removing: {}", s)
        replicator ! Update(DataKey, ORSet.empty[String], WriteLocal)(_ - s)
      }

    case _: UpdateResponse[_] ⇒ // ignore

    case c @ Changed(DataKey) ⇒
      val data = c.get(DataKey)
      log.info("Current elements: {}", data.elements)

  }

  override def postStop(): Unit = tickTask.cancel()

}