package com.actor_example.service

import akka.actor.{Actor, Props}
import com.actor_example.service.PingActor.{HelloActor, IntegerActor, TellCounter}

class PingActor(startFrom: Int) extends Actor {

//  private var counter = 0

  /*
   * когда этому актеру приходит сообщение типа String - можно написать - 'Hello $str'
   */
//  override def receive: Receive = {
//    case "tell counter" =>
////      println(s"counter is $counter")
//      context.become(printer)
//    case str: String =>
//      counter = counter + 1
//      println(s"Hello $str") // здесь PartialFunction (частичная функция) - может быть все что угодно...
//    case i: Int =>
//      counter = counter + 1
//      println(s"Got $i integer")
//  }

  override def receive: Receive = printer(startFrom)

//  def printer(): Receive = {
//    case "untell" =>
//      context.become(receive)
//    case str: String =>
//      counter = counter + 1
//      println(s"Counter $str")
//  }

//  def printer(counter: Int): Receive = {
//    case "tell counter" =>
//      sender() ! s"counter is $counter" // отправить сообщение обратно отправителю
//    case str: String =>
//      context.become(printer(counter + 1))
//      println(s"Counter $str")
//    case i: Int =>
//      context.become(printer(counter + 1))
//      println(s"Got $i integer")
//  }

  def printer(counter: Int): Receive = {
    case TellCounter =>
      sender() ! s"counter is $counter" // отправить сообщение обратно отправителю
    case HelloActor(str) =>
      context.become(printer(counter + 1))
      println(s"Counter $str")
    case IntegerActor(i) =>
      context.become(printer(counter + 1))
      println(s"Got $i integer")
  }

  case class SomeMessage()

  /*
   * Создатели Akka наоборот говорят что система акторов это такое TRUE-OOP программирование, потому что у нас действительно есть объекты которые между собой обмениваются сообщениями...
   *
   * Нельзя использовать конструктор напрямую чтобы создать актера, потому-что вылетит ошибка: ActorInitializationException You cannot create an instance...
   *   val pingActor = new PingActor
   * Можно использовать только фабричный метод 'actorOf'
   *   val actorSystem = ActorSystem.create("System")
   *   val pingActor: ActorRef = actorSystem.actorOf( Props[PingActor],"pingActor" ) // 'ActorRef' - означает ссылка на актера, эта ссылка сериализуема и Akka сама знает как эту ссылку отправить по сети...
   * раньше в версии 'Akka-2.5' это был 'java.Serialization'
   * сейчас в версии 'Akka-2.6' по умолчанию это 'google.Protoba'
   *   pingActor.path = "/user/pingActor"
   * если в каком-нибудь актере произойдет Exception, тогда у нас этот актер умрет и на его месте создастся новый такой-же актер... и сможет дальше продолжать обрабатывать его сообщения
   *
   * После того как актер будет создан, программа находится в ожидании и свою работу не прекращает, как это обычно бывает, когда заканчивает выполнение кода
   * Для того чтобы сказать программе - прекратить работу - нужно
   *   actorSystem.terminate()
   *
   * + Существует - 'корневой актер'
   *   - 'user' - это наш актер
   *   - 'system' - это системный актер и его кухня от нас скрыта...
   *
   * Если актеру придет сообщение, а обработать его нечем - для этого существует специальный актер 'dead-letters' и все что не обработалось - все попадет в 'dead-letters'
   *
   * Чтобы передать нашему актеру какое-нибудь сообщение нужно:
   *    pingActor.tell( msg="Anton", ActorRef.noSender )
   * другой способ (алиас) передать актеру сообщение:
   *    pingActor.!( message="Anton" )
   *    pingActor ! "Anton"     pingActor ! HelloActor("Anton")
   *    // pingActor ! SomeMessage()
   *    pingActor ! "tell counter"       pingActor ! TellCounter
   *
   * Все что было отправлено в порядке - все в том же порядке придет актер
   * (порядок передаваемых актеру сообщений перемешаться не может )
   *
   * Любое сообщение из 'dead-letters' означает что наш актер что-то не обработал.
   * От кого это сообщение пришло?
   * Как достать сообщение из очереди 'dead-letters'?
   * Какой тип имеет сообщение?
   * И поэтому, обычно, найти такого актера от которого пришло такое сообщение очень трудно, а порой даже не возможно... и это есть величайшая проблема при работе с актерами!
   *
   * Как менять состояние актера:
   * Как отправить что-то обратно актеру...:
   * Как спросить у актера о его состоянии (это плохой способ №2):
   *   implicit val timeout = Timeout(1.seconds)
   *   pingActor.ask(message="tell counter")
   *   val futureAny: Future[Any] = pingActor ? "tell counter" // здесь нам вернулся Future[Any]
   * но теперь здесь уже другая проблема - как узнать что такое Any? как узнать что этот актер вообще вернул - и это проблема которую решает actor-typed ?
   *   futureAny.map(println(_))
   * более чем в контролерах этот Ask-паттерн спрашивать нельзя
   *
   *    pingActor.forward( msg="Anton" )(implicit context: ActorContext)
   * метод 'forward' - он работает как 'tell', но только еще имплисивно передает параметр sender-а
   * в итоге один актер может передать 1, потом 2, потом 3, потом 4, потом 5 актеру параметр. А в 5 актере будет вычислен результат и обратно отправлен 1-актеру...
   * Все эти вычисления: tell, forward - являются асинхронными
   *
   * 01:53:00 / 03:29:00
   *
   */
}

/*
 * Обычно в коде такой актер используют через класс-компаньйон
 * А уже где-то в методе main использовать вот так использовать вызов для создания актера
 *   val pingActor: PingActor = system.actorOf( PingActor.props(startFrom=42), name="pingActor" )
 */
object PingActor {

  // считается хорошая практика предъявить отдельно  (02:13:00 / 03:29:00)
  sealed trait PingActorEvent
  case object TellCounter extends PingActorEvent
  case class HelloActor(name: String) extends PingActorEvent
  case class IntegerActor(i: Int) extends PingActorEvent

  def props(startFrom: Int = 0): Props = Props(new PingActor(startFrom))
}