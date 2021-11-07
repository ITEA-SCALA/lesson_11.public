package com.actor_example.service

import akka.actor.Actor
import com.actor_example.data.{Envelope, OpFailure, OpSuccess}
import com.actor_example.dao.FileUtil

/**
  * Actor taking care to verify whether the message sender is registered, and therefore allowed to send a message
  */
class AuthenticateActor extends Actor {

  /**
    * Path to the logActor
    */
  val logActor = context.actorSelection("/user/logActor")
  /**
    * Path to the senderActor
    */
  val senderActor = context.actorSelection("/user/senderActor")

  override def receive: Receive = {
    case msg : Envelope =>
      val users = FileUtil.loadUsersFromFile()                  // Loading users from file
      if(users.contains(msg.senderId)) {                        // If users contain the sender ID, we can proceed
        logActor ! "Message from " + msg.senderId + " accepted"
        sender() ! OpSuccess("Operation accepted")              // Respond a success message to the sender (async)
        senderActor ! msg                                       // The message is passed to the senderActor (async)
      } else {
        logActor ! "Message from " + msg.senderId + " refused"  // User is not among the registered users
        sender() ! OpFailure("Sender is not registered")        // Respond a failure message to the sender (async)
      }
  }
}
