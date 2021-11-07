package com.actor_example.service

import akka.actor.Actor
import com.actor_example.data.Envelope
import com.actor_example.dao.FileUtil

/**
  * Actor handling the the messages being pushed to the system
  */
class SenderActor extends Actor {

  override def receive: Receive = {
    case msg : Envelope =>
        FileUtil.appendMessageToFile(msg)
  }

}
