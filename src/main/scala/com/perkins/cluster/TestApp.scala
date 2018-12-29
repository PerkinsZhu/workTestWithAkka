package com.perkins.cluster

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by PerkinsZhu on 2018/12/29 17:15
  **/
object TestApp {
  def main(args: Array[String]): Unit = {
    ServerActor.startTask(2551)
    ServerActor.startTask(2552)
    ServerActor.startTask(2553)
    ServerActor.startTask(2554)
    ServerActor.startTask(2555)
    ClientActor.startTask()
  }


}
