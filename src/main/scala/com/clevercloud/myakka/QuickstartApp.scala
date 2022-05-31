package com.clevercloud.myakka

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.clevercloud.myakka.configuration.{BasicAuthConfig, PostgresConfig, SentryConfig}
import com.typesafe.config.ConfigFactory

import io.sentry.Sentry
import scala.util.Failure
import scala.util.Success

//#main-class
object QuickstartApp {
  //#start-http-server
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_], sentryIsActivated: Boolean): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val futureBinding = Http().newServerAt("0.0.0.0", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        val infoLog = s"Server online at http://${address.getHostString}:${address.getPort}/"
        system.log.info(infoLog)
        sendMessageToSentry("info", infoLog)
      case Failure(ex) =>
        val errorLog = s"Failed to bind HTTP endpoint, terminating system: $ex"
        system.log.error(errorLog)
        sendMessageToSentry("error", errorLog)
        system.terminate()
    }
  }

  def sendMessageToSentry(status: String, message: String)(implicit sentryIsActivated: Boolean): Unit = {
    if (sentryIsActivated) {
      Sentry.getContext().addTag("status", status)
      Sentry.capture(message)
      Sentry.getContext().clear()
    }
  }

  //#start-http-server
  def main(args: Array[String]): Unit = {
    // configuration
    val config = ConfigFactory.load("application.conf")
    val pgConfig = PostgresConfig(config)
    val authConfig = BasicAuthConfig(config)

    // Init Sentry
    val sentryConfig = SentryConfig(config)
    val sentryIsActivated = if (sentryConfig.sentryDsn != "" ) true else false
    if (sentryIsActivated) {
      Sentry.init(sentryConfig.sentryDsn)
    }

    //#server-bootstrapping
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val userRegistryActor = context.spawn(UserRegistry(pgConfig), "UserRegistryActor")
      context.watch(userRegistryActor)

      val routes = new UserRoutes(userRegistryActor, authConfig)(context.system)
      startHttpServer(routes.userRoutes)(context.system, sentryIsActivated)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
    //#server-bootstrapping
  }
}
//#main-class
