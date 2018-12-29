package com.perkins.cluster

import akka.actor.{Actor, ActorLogging}
import akka.remote.transport.AkkaPduCodec
import akka.routing.FromConfig

/**
  * Created by PerkinsZhu on 2018/12/29 17:16
  **/
class ClientActor extends Actor with ActorLogging {
  override def preStart(): Unit = {
    log.info("pre start ClientActor")
    val server = context.actorOf(FromConfig.props(), name = "factorialBackendRouter")
    server ! Message("client-01", "i am client-01")
  }

  override def receive: Receive = {
    case msg: Message => {
      log.info(s"receive message from ${msg.name},conten:${msg.msg}")
    }
  }
}
