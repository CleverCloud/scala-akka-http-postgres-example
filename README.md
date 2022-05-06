This is an example Akka HTTP application that works with PostgreSQL on Clever Cloud.

# Features
Project generated from Akka Github [quick start template](https://github.com/akka/akka-http-quickstart-scala.g8).

This service provides the ability to manage a simple in-memory user registry exposing 4 routes:
- List all users
- Get a specific user
- Create a user
- Delete a user

A thorough  description of the cURL commands may be found [here](https://developer.lightbend.com/guides/akka-http-quickstart-scala/#cURL-commands)

A few functionalities have been added on top of the original example:
- store the registry into a PostgreSQL database
- read database parameters from  Clever Cloud standard environment variables
- add basic authentication (`foo:bar`)

# Configuration
Configuration parameters are located in the [application.conf](src/main/resources/application.conf)
You can edit parameters for
- basic-auth
- PostgreSQL database

# Database creation
The program uses [Flyway sbt plugin](https://github.com/flyway/flyway-sbt) in order to create the database table automatically.
It requires a hook run before the program starts. Just set in the application environment the variable `CC_PRE_RUN_HOOK` with the value `sbt flywayMigrate`

# Installation
To install it, simply fork this repository and create an application from your GitHub repo. Then create a PostgreSQL add-on and link it to your application, either via the Clever Tools CLI or via the Clever Cloud console.

That's it, the application will use the environment variables to connect to the PostgreSQL DB.