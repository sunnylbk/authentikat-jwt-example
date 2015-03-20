package com.sunnylbk.jwt.client

import akka.actor.ActorSystem
import akka.event.Logging
import akka.io.IO
import akka.pattern.ask
import spray.can.Http
import spray.client.pipelining._
import spray.json.DefaultJsonProtocol
import spray.util._

import scala.concurrent.duration._
import scala.util.{Failure, Success}


case class Person(name: String, age: Int)

object CustomJsonProtocol extends DefaultJsonProtocol {
  implicit val personFormat = jsonFormat2(Person.apply)
}

object Client extends App {
  implicit val system = ActorSystem("simple-spray-client")
  import com.sunnylbk.jwt.client.Client.system.dispatcher // execution context for futures below
  val log = Logging(system, getClass)

  import com.sunnylbk.jwt.client.CustomJsonProtocol._
  import spray.httpx.SprayJsonSupport._
  
  val pipeline = sendReceive ~> unmarshal[Person]
  
  val responseFuture = pipeline {
    Get("http://localhost:8181/ping")
  }
  
  def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(1.second).await
    system.shutdown()
  }
}
