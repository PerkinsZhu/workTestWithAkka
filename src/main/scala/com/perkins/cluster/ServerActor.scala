package com.perkins.cluster

import akka.actor.{Actor, ActorLogging}

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
