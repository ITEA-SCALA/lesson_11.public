package com.actor_example

import akka.actor.{ActorSystem, Props}
import com.actor_example.controller.HttpActor
import com.actor_example.dist.StartWebServerCommand
import com.actor_example.service.{AuthenticateActor, LogActor, RegisterActor, SenderActor}

/**
 * Пример работы с моделью актеров
 * ***
 * Это простая реализация 'akka.actor' (не типизированных актеров 'akka-actor-typed')
 * Еще акторы можно запускать на разных кластерах (в текущем примере применяется кластер по умолчанию - локальный)
 */
object Application extends App {

    // это фабрика для создания актеров
    val actorSystem = ActorSystem.create("System")

    /*
     * Контроллер (роутер)
     * этот актер отвечает за получение клиентски данных...
     */
    val httpActor = actorSystem.actorOf( Props[HttpActor],"httpActor" )
    httpActor ! StartWebServerCommand

    /*
     * Сервисы реализованные на акторах (дальше будем создавать актеры которые участвуют в бизнес-процессе):
     * эти актеры отвечают за обработку клиентских данных...
     * 1. сервис для регистрации нового пользователя в системе
     */
    val registerActor     = actorSystem.actorOf( Props[RegisterActor], "registerActor" )
    val authenticateActor = actorSystem.actorOf( Props[AuthenticateActor], "authenticateActor" )
    // 2. сервис для отправки сообщений пользователю
    val senderActor       = actorSystem.actorOf( Props[SenderActor], "senderActor" )
    // 3. этот сервис отвечает только за журналирование (печатает форматированный вывод в логи...)
    val logActor          = actorSystem.actorOf( Props[LogActor], "logActor" )

}
