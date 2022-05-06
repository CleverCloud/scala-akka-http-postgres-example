import sbt.Keys.mappings

lazy val akkaHttpVersion = "10.2.9"
lazy val akkaVersion = "2.6.19"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

mappings in (Compile, packageDoc) := Seq()

lazy val DB_HOST=sys.env.get("POSTGRESQL_ADDON_HOST").getOrElse("localhost")
lazy val DB_PORT=sys.env.get("POSTGRESQL_ADDON_PORT").getOrElse("5432")
lazy val DB_NAME=sys.env.get("POSTGRESQL_ADDON_DB").getOrElse("postgres")
lazy val DB_USER=sys.env.get("POSTGRESQL_ADDON_USER").getOrElse("login")
lazy val DB_PASS=sys.env.get("POSTGRESQL_ADDON_PASSWORD").getOrElse("pass")

flywayUrl := s"jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
flywayUser := DB_USER
flywayPassword := DB_PASS

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(FlywayPlugin)
  .settings(
  inThisBuild(
    List(organization := "com.clevercloud", scalaVersion := "2.13.4")
  ),
  name := "myakka",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.1.4" % Test,
    // Start with this one
    "org.tpolecat" %% "doobie-core" % "1.0.0-RC1",
    // And add any of these as needed
    "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC1", // Postgres driver 42.3.1 + type mappings.
  )
)
