package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

object ActorLifecycle extends App{

  case object StartChild
  class LifecycleActor extends Actor with ActorLogging{

    override def preStart(): Unit = log.info(s"I am starting")

    override def postStop(): Unit = log.info("I have stopped")
    override def receive: Receive = {
      case StartChild =>
        context.actorOf(Props[LifecycleActor],"child")
    }
  }

  val system = ActorSystem("LifecycleDemo")
/*  val parent = system.actorOf(Props[LifecycleActor],"parent")
  parent ! StartChild
  parent ! PoisonPill*/

  /**
   * restart
   */
  object Fail
  object FailChild
  class Parent extends Actor{
   private val child = context.actorOf(Props[Child],"supervisedChild")

    override def receive: Receive = {
      case FailChild => child ! Fail
    }
  }

  class Child extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("supervised child started")

    override def postStop(): Unit = log.info("supervised child stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"supervised actor restarting because of ${reason.getMessage}")

    override def postRestart(reason: Throwable): Unit =
      log.info("supervised actor restarted")

    override def receive: Receive = {
      case Fail =>
        log.warning("child will fail now")
        throw new RuntimeException("I failed")
    }
  }

  val superviser = system.actorOf(Props[Parent], "supervisor")
  superviser ! FailChild
}
