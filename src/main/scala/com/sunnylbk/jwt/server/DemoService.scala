package com.sunnylbk.jwt.server

import akka.actor.{Actor, _}
import akka.pattern.ask
import akka.util.Timeout
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import spray.can.Http
import spray.can.server.Stats
import spray.http.HttpMethods._
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.unmarshalling._
import spray.json._
import spray.util._


import scala.concurrent.duration._
import scala.util.Try


case class BasicCredentials(username: String, password: String)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val basicCredentialsFormats = jsonFormat2(BasicCredentials)
}

class DemoService extends Actor with ActorLogging with DefaultJsonProtocol{
  implicit val timeout: Timeout = 1.second // for the actor 'asks'

  import context.dispatcher
  import spray.httpx.SprayJsonSupport._
  import MyJsonProtocol._

  def jsonResponseEntity = HttpEntity(
    contentType = ContentTypes.`application/json`,
    string = JsObject("message" -> JsString("Hello, World!")).compactPrint)

  val header = JwtHeader("HS256")
  val superSecretKey = "34O*&$#LKDFS>VVVDSLKJ#)(@$%LJK:K;lkdfsamwer.s;loeql;"


  private def getUserRole(jwt: String) : Option[String] = {
    jwt match {
      case JsonWebToken(headerValue, claimsSet, signature) =>
        claimsSet.asSimpleMap.map(_.get("role")).toOption.flatMap(identity)
      case x =>
        None
    }
  }

  def receive = {
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(GET, Uri.Path("/"), _, _, _) => sender ! index

    case r@HttpRequest(POST, Uri.Path("/login"), headers, entity, protocol) =>
      entity.as[BasicCredentials] match {
        case Left(a) => sender ! HttpResponse(entity = "Invalid format for credentials")
        case Right(credentials) =>
          val role = if(credentials.username == "admin") "admin" else "user"
          val claimsSet = JwtClaimsSet(Map("userId" -> "5699198889", "role" -> role))
          val jwt = JsonWebToken(header, claimsSet, superSecretKey)
          sender ! HttpResponse(entity = jwt)
      }


    case HttpRequest(GET, Uri.Path("/ping"), headers, _, _) =>
      headers.find(_.name == "Authorization") match {
        case Some(authHeader) =>
          Try(JsonWebToken.validate(authHeader.value, superSecretKey)).getOrElse(false) match {
            case true =>
              sender ! HttpResponse(entity= "PONG!")
            case false =>
              sender ! HttpResponse(status = StatusCodes.Unauthorized, entity = "Invalid or expired authorization data. Please re login")
          }
        case None => sender ! HttpResponse(status = StatusCodes.Unauthorized, entity = "No authorization sent!")
      }

    case HttpRequest(GET, Uri.Path("/server-stats"), headers, _, _) =>
      val client = sender
      headers.find(_.name == "Authorization") match {
        case Some(authHeader) =>
          Try(JsonWebToken.validate(authHeader.value, superSecretKey)).getOrElse(false) match {
            case true =>
              getUserRole(authHeader.value).map(_ == "admin").getOrElse(false) match {
                case true =>
                  context.actorFor("/user/IO-HTTP/listener-0") ? Http.GetStats onSuccess {
                    case x: Stats => client ! statsPresentation(x)
                  }
                case false => client ! HttpResponse(status = StatusCodes.Unauthorized, entity = "Only admin can access server stats")
              }

            case false =>
              sender ! HttpResponse(status = StatusCodes.Unauthorized, entity = "Invalid or expired authorization data. Please re login")
          }
        case None => sender ! HttpResponse(status = StatusCodes.Unauthorized, entity = "No authorization sent!")
      }

    case r@HttpRequest(POST, Uri.Path("/file-upload"), headers, entity: HttpEntity.NonEmpty, protocol) =>
      sender ! "got POST"

    case _: HttpRequest => sender ! HttpResponse(status = 404, entity = "Unknown resource!")
  }

  lazy val index = HttpResponse(
    entity = HttpEntity(`text/html`,
      <html>
        <body>
          <h1>Say hello to
            <i>spray-can</i>
            !</h1>
          <p>Defined resources:</p>
          <ul>
            <li>
              <a href="/ping">/ping</a>
            </li>
            <li>
              <a href="/server-stats">/server-stats</a>
            </li>
          </ul>
        </body>
      </html>.toString()
    )
  )

  def statsPresentation(s: Stats) = HttpResponse(
    entity = HttpEntity(`text/html`,
      <html>
        <body>
          <h1>HttpServer Stats</h1>
          <table>
            <tr>
              <td>uptime:</td> <td>
              {s.uptime.formatHMS}
            </td>
            </tr>
            <tr>
              <td>totalRequests:</td> <td>
              {s.totalRequests}
            </td>
            </tr>
            <tr>
              <td>openRequests:</td> <td>
              {s.openRequests}
            </td>
            </tr>
            <tr>
              <td>maxOpenRequests:</td> <td>
              {s.maxOpenRequests}
            </td>
            </tr>
            <tr>
              <td>totalConnections:</td> <td>
              {s.totalConnections}
            </td>
            </tr>
            <tr>
              <td>openConnections:</td> <td>
              {s.openConnections}
            </td>
            </tr>
            <tr>
              <td>maxOpenConnections:</td> <td>
              {s.maxOpenConnections}
            </td>
            </tr>
            <tr>
              <td>requestTimeouts:</td> <td>
              {s.requestTimeouts}
            </td>
            </tr>
          </table>
        </body>
      </html>.toString()
    )
  )

}
