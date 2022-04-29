package com.clevercloud.myakka

//#user-registry-actor
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.immutable
import doobie._
import doobie.implicits._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.clevercloud.myakka.configuration.PostgresConfig

//#user-case-classes
final case class User(name: String, age: Int, countryOfResidence: String)
final case class Users(users: immutable.Seq[User])
//#user-case-classes


object UserRegistry {
  // actor protocol
  sealed trait Command
  final case class GetUsers(replyTo: ActorRef[Users]) extends Command
  final case class CreateUser(user: User, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetUser(name: String, replyTo: ActorRef[GetUserResponse]) extends Command
  final case class DeleteUser(name: String, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetUserResponse(maybeUser: Option[User])
  final case class ActionPerformed(description: String)

  def apply(pgConfig: PostgresConfig): Behavior[Command] = {
    val xa = transactor(pgConfig)
    dbCreatePublicRegistry(xa)
    registry(xa)
  }

  type Transactor = doobie.Transactor.Aux[IO, Unit]
  def transactor(pgConfig: PostgresConfig): Transactor = {
    Transactor.fromDriverManager[IO](
      "org.postgresql.Driver", pgConfig.url, pgConfig.user, pgConfig.pass
    )
  }

  def dbCreatePublicRegistry(xa: Transactor): Unit = {
    sql"""
    CREATE TABLE IF NOT EXISTS user_registry (
      id serial PRIMARY KEY,
      name VARCHAR ( 255 ) UNIQUE NOT NULL,
      age INT NOT NULL,
      country VARCHAR ( 255 ) NOT NULL
    )
    """.
    update.
    run.
    transact(xa).
    unsafeRunSync
  }

  def dbGetUsers(xa: Transactor): Users = {
    Users(
      sql"SELECT name, age, country FROM user_registry".
      query[User].
      to[List].
      transact(xa).
      unsafeRunSync
    )
  }

  def dbGetUser(xa: Transactor, name: String): Option[User] = {
    sql"SELECT name, age, country FROM user_registry WHERE name = ${name}".
      query[User].
      option.
      transact(xa).
      unsafeRunSync
  }

  def dbCreateUser(xa: Transactor, user: User): Unit = {
    sql"INSERT INTO user_registry (name, age, country) VALUES(${user.name}, ${user.age}, ${user.countryOfResidence})".
      update.
      withUniqueGeneratedKeys[Int]("id").
      transact(xa).
      unsafeRunSync
  }

  def dbDeleteUser(xa: Transactor, name: String): Unit = {
    sql"DELETE FROM user_registry WHERE name = ${name}".
      update.
      run.
      transact(xa).
      unsafeRunSync()
  }

  private def registry(xa: Transactor): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        replyTo ! dbGetUsers(xa)
        Behaviors.same
      case CreateUser(user, replyTo) =>
        replyTo ! ActionPerformed(s"User ${user.name} created.")
        dbCreateUser(xa, user)
        Behaviors.same
      case GetUser(name, replyTo) =>
        replyTo !  GetUserResponse(dbGetUser(xa, name))
        Behaviors.same
      case DeleteUser(name, replyTo) =>
        replyTo ! ActionPerformed(s"User $name deleted.")
        dbDeleteUser(xa, name)
        Behaviors.same
    }
}
//#user-registry-actor
