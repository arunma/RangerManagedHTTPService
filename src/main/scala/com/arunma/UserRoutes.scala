package com.arunma

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.http.scaladsl.server.{Directive, RequestContext, Route}
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import scala.concurrent.Future
import com.arunma.UserRegistryActor._
import akka.pattern.ask
import akka.util.Timeout
import com.arunma.ServiceRegistryActor.GetServices
import com.arunma.ranger.RangerAuthorizer

trait UserRoutes extends JsonSupport {

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[UserRoutes])

  // other dependencies that UserRoutes use
  def userRegistryActor: ActorRef
  def serviceRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  def isRangerAuthorized(path: String, httpMethod: String, userName: String): Boolean = {
    println(s"isRangerAuthorized params : $path:${httpMethod.toLowerCase}:$userName:")
    RangerAuthorizer.authorize(path, httpMethod.toLowerCase, userName)
  }

  lazy val serviceRoutes: Route = {
    path("services") {
      get {
        val services: Future[String] = (serviceRegistryActor ? GetServices).mapTo[String]
        complete(services)
      }
    }
  }
  lazy val userRoutes: Route =
    headerValueByName("username") { userName =>
      extractMethod { method =>
        pathPrefix("users") {
          //extractMatchedPath { matchedPath => //Comes with a / as a prefix
          authorize(isRangerAuthorized("users", method.name(), userName)) {
            concat(
              pathEnd {
                concat(
                  get {
                    val users: Future[Users] =
                      (userRegistryActor ? GetUsers).mapTo[Users]
                    complete(users)
                  },
                  post {
                    entity(as[User]) { user =>
                      val userCreated: Future[ActionPerformed] =
                        (userRegistryActor ? CreateUser(user)).mapTo[ActionPerformed]
                      onSuccess(userCreated) { performed =>
                        log.info("Created user [{}]: {}", user.name, performed.description)
                        complete((StatusCodes.Created, performed))
                      }
                    }
                  })
              },
              path(Segment) { name =>
                concat(
                  get {
                    val maybeUser: Future[Option[User]] =
                      (userRegistryActor ? GetUser(name)).mapTo[Option[User]]
                    rejectEmptyResponse {
                      complete(maybeUser)
                    }
                  },
                  delete {
                    val userDeleted: Future[ActionPerformed] =
                      (userRegistryActor ? DeleteUser(name)).mapTo[ActionPerformed]
                    onSuccess(userDeleted) { performed =>
                      log.info("Deleted user [{}]: {}", name, performed.description)
                      complete((StatusCodes.OK, performed))
                    }
                  })
              })
          }
        }
      }
    }
}
