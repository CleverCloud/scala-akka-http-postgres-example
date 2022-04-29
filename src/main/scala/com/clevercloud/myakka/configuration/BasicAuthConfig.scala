package com.clevercloud.myakka.configuration

import com.typesafe.config.Config

final case class BasicAuthConfig(user: String, password: String)

object BasicAuthConfig {
  def apply(config: Config): BasicAuthConfig = {
    BasicAuthConfig(
      config.getString("app.basic-auth.user"),
      config.getString("app.basic-auth.password")
    )
  }
}