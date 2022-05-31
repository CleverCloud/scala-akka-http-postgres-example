package com.clevercloud.myakka.configuration
import com.typesafe.config.Config

object SentryConfig {
  def apply(config: Config): SentryConfig = {
    val sentryDsn = config.getString("app.sentry.dsn")
    SentryConfig(sentryDsn)
  }
}

final case class SentryConfig(sentryDsn: String)
