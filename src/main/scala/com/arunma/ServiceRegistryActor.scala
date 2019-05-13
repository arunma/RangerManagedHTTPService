package com.arunma

import java.util.Date

import akka.actor.{Actor, ActorLogging, Props}

object ServiceRegistryActor {
  final case object GetServices

  def props: Props = Props[ServiceRegistryActor]
}

class ServiceRegistryActor extends Actor with ActorLogging {
  import ServiceRegistryActor._

  def receive: Receive = {
    case GetServices =>
      println (s"Returning results from GetServices at ${new Date}")
      sender() ! "users,others"
  }
}
