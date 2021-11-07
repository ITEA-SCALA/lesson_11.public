package com.actor_example.controller

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.{Http, HttpExt}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import com.actor_example.data.JsonSupport._
import akka.util.Timeout
import com.actor_example.data.{Envelope, OpFailure, OpSuccess, Registration}
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import com.actor_example.dist._

/**
  * Actor taking care of configuring and starting the web server
  */
class HttpActor extends Actor {

  /*
   * Поскольку Akka-HTTP основан на Akka-Stream, нам также потребуется предоставить ActorMaterializer()
   *
   * Затем мы добавляем традиционные компоненты Akka Actor System, а именно:
   * - саму 'ActorSystem'
   * - за которой следует 'ActorMaterializer'
   * - и 'ExecutionContext' или 'dispatcher'
   */
  implicit val system: ActorSystem                = context.system
  implicit val materializer: ActorMaterializer    = ActorMaterializer() // TODO: akkaVersion=2.5.23; akkaHttpVersion=10.1.8;
  implicit val executionContext: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout                   = Timeout(10, TimeUnit.SECONDS)

  val conf: Config       = ConfigFactory.load()
  val restConfig: Config = conf.getConfig("config.rest")
  val interface = restConfig.getString("interface")
  val port      = restConfig.getString("port")

  val registerActor     = context.actorSelection("/user/registerActor")     // Путь к актеру, который занимается регистрацией пользователя.
  val authenticateActor = context.actorSelection("/user/authenticateActor") // Путь к актеру, который заботится об аутентификации входящих сообщений.

  var http: HttpExt = null
  var binding: Future[ServerBinding] = null

  /*
   * Для реализации трейда 'Actor' нужно заимплементировать только один метод 'receive'
   * 'receive' - означает что мы достали ожидающее сообщение из почтового ящика и будем его обрабатывать
   * тип ответа 'Receive' - это PartialFunction (частичная функция)
   *
   * 1. Функция которая возвращает пустой тип 'Any => Unit' (void) - это side-еффект
   *    И считается хорошо-бы его обернуть во что-нибудь, например Future[Unit] или IO[Unit] или Task[Unit]
   * 2. тип 'Any => Unit' (Any) - это может быть все что угодно...
   */
  override def receive: Receive = {
    // Если получена команда `StartWebServerCommand`, тогда запустить веб-сервер.
    case StartWebServerCommand =>
      if (http == null)
        startWebServer

    // Если получена команда `StopWebServerCommand`, тогда остановите веб-сервер.
    case StopWebServerCommand =>
      if (binding != null)
        Await.result(binding, 10.seconds)
          .terminate(hardDeadline = 3.seconds)
  }

  /**
    * Configures and starts a web server
    */
  def startWebServer {
    val routes: Route =
      // The endpoint который принимает сообщение для доставки
      path("consume") {
        post {
          entity(as[Envelope]) { envelope =>
            // On success, пересылает конверт to the authenticateActor и ждите его вердикта.
            onSuccess(authenticateActor ? envelope) { // TODO: implicit value for parameter timeout: akka.util.Timeout
              // Если AuthenticateActor возвращает OpSuccess, значит, все в порядке, и мы печатаем сообщение.
              case res : OpSuccess => complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, res.toJson.prettyPrint))
              // Если AuthenticateActor возвращает OpFailure, значит, что-то не в порядке и тогда мы печатаем сообщение.
              case res : OpFailure => complete(StatusCodes.BadRequest,HttpEntity(ContentTypes.`application/json`, res.toJson.prettyPrint))
            }
          }
        }
      } ~
      // The endpoin который регистрирует пользователя
      path("register") {
        post {
          entity(as[Registration]) { registration =>
                // Отправляет сообщение registerActor и продолжает работать дальше, не дожидаясь ответа
                registerActor ! registration
                // Возвращает подтверждающее сообщение
                complete(HttpEntity(ContentTypes.`application/json`, "{\"done\":true}"))
          }
        }
      }

    // Отправляет асинхронное сообщение в logActor, чтобы сообщить, что веб-сервер скоро запустится
    context.actorSelection("/user/logActor") ! s"Starting HTTP Server $interface:$port"
    // Start and bind the web server
    http = Http()
    binding = http.bindAndHandle(routes, interface = interface, port = port.toInt)
  }
}
