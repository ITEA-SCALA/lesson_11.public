package com.actor_example.dao

import com.actor_example.data.{Envelope, Registration}

import java.io.{File, FileWriter}
import com.actor_example.data.JsonSupport._
import spray.json._

import scala.io.Source

/**
  * Utilities: отвечает за сохранение и поиск клиентских данных (в файл...users.json)
  * (в будущем, для этих целей, можно использовать базу данных)
  */
object FileUtil {

  /**
    * File where users get written to
    */
  val usersFile = new File("users.json")

  /**
    * Directory where messages are stored
    */
  val messagesDirectory = new File("logs")

  /**
    * Loads and deserializes users from a file
    * @param file a file
    * @return the deserialized users
    */
  def loadUsersFromFile(): Map[String, Registration] = {
    if (usersFile.exists()) {
      val source = Source.fromFile(usersFile)
      val data = source.mkString
      if(data.isEmpty)
        return Map[String,Registration]()
      source.close()
      val dataFromFile: Map[String, Registration] = data.parseJson.convertTo[Map[String, Registration]]
      return dataFromFile
    }
    Map[String, Registration]()
  }

  /**
    * Serializes and saves users to a file
    * @param file a file
    * @param items a map of users
    */
  def saveUsersToFile(items: Map[String, Registration]): Unit = {
    if (!usersFile.exists())
      usersFile.createNewFile()
    val fileWriter = new FileWriter(usersFile)
    fileWriter.write( items.toJson.prettyPrint )
    fileWriter.close()
  }

  /**
    * Serializes and appends a SimpleMessage to a file
    * @param file a file
    * @param message a SimpleMessage
    */
  def appendMessageToFile(message: Envelope): Unit = {
    val messageFile = new File( messagesDirectory.getAbsolutePath + File.separator + message.recipient + ".log" )
    if (!messageFile.exists())
      messageFile.createNewFile()
    val fileWriter = new FileWriter( messageFile,true )
    fileWriter.append( message.toJson.compactPrint + "\n" )
    fileWriter.close()
  }
}
