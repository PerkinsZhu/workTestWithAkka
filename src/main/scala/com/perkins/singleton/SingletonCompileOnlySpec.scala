package com.perkins.singleton


import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Props, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.SingletonActor

import scala.concurrent.duration._

/**
  * Created by PerkinsZhu on 2019/1/4 13:28
  **/
object SingletonCompileOnlySpec  extends App {

  val system = ActorSystem(Behaviors.empty, "Singleton")

  //#counter
  trait CounterCommand
  case object Increment extends CounterCommand
  final case class GetValue(replyTo: ActorRef[Int]) extends CounterCommand
  case object GoodByeCounter extends CounterCommand

  def counter(value: Int): Behavior[CounterCommand] =
    Behaviors.receiveMessage[CounterCommand] {
      case Increment ⇒
        counter(value + 1)
      case GetValue(replyTo) ⇒
        replyTo ! value
        Behaviors.same
      case GoodByeCounter ⇒
        // Do async action then stop
        Behaviors.stopped
    }
  //#counter

  //#singleton
  import akka.cluster.typed.ClusterSingleton


  // 使用 singletonManager 创建 单例 actor
  val singletonManager = ClusterSingleton(system)
  // Start if needed and provide a proxy to a named singleton
  val proxy: ActorRef[CounterCommand] = singletonManager.init(
    SingletonActor(Behaviors.supervise(counter(0))
      .onFailure[Exception](SupervisorStrategy.restart), "GlobalCounter")
  )

  proxy ! Increment
  //#singleton

  //#stop-message
  val singletonActor = SingletonActor(counter(0), "GlobalCounter").withStopMessage(GoodByeCounter)
  singletonManager.init(singletonActor)
  //#stop-message

  //#backoff
  val proxyBackOff: ActorRef[CounterCommand] = singletonManager.init(
    SingletonActor(Behaviors.supervise(counter(0))
      .onFailure[Exception](SupervisorStrategy.restartWithBackoff(1.second, 10.seconds, 0.2)), "GlobalCounter")
  )
  //#backoff
}