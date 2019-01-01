package com.perkins.cluster

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by PerkinsZhu on 2018/12/29 17:15
  **/
object TestApp {
  def main(args: Array[String]): Unit = {
    2551 to 2556 foreach(i =>{
      ServerActor.startTask(i)
    })

    ClientActor.startTask()
  }


}
