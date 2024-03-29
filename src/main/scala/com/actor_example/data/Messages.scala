package com.actor_example.data

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * Message envelope
  * @param senderId the sender ID
  * @param message the SimpleMessage
  */
case class Envelope(senderId : String, recipient : String, message : SimpleMessage)

/**
  * The SimpleMessage
  * @param recipient the recipient of the message
  * @param subject the subject
  * @param text the text
  */
case class SimpleMessage(subject : String, text : String)

/**
  * The registration message
  * @param id id of the registrant
  * @param fullName the full name of the registrant
  */
case class Registration(id : String, fullName : String)

/**
  * Success message
  * @param message the message
  */
case class OpSuccess(message : String)

/**
  * Failure message
  * @param message the message
  */
case class OpFailure(message : String)

/**
  * Json formatters
  */
object JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val simpleMessageFormat = jsonFormat2(SimpleMessage)
  implicit val envelopeFormat = jsonFormat3(Envelope)
  implicit val registrationFormat = jsonFormat2(Registration)
  implicit val opSuccessFormat = jsonFormat1(OpSuccess)
  implicit val opFailureFormat = jsonFormat1(OpFailure)
}