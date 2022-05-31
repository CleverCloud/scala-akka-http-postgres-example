#!/bin/bash

# Load version from Commit ID set by Clever Cloud env var (https://www.clever-cloud.com/doc/develop/env-variables/#special-environment-variables)
VERSION="$COMMIT_ID"

# Migrate the linked database with flyway
sbt flywayMigrate

# Download sentry CLI and create release on Sentry (or Glitchtip)
npm install @sentry/cli
./node_modules/.bin/sentry-cli releases new "${VERSION}"
./node_modules/.bin/sentry-cli releases set-commits --auto "${VERSION}"
./node_modules/.bin/sentry-cli releases finalize "${VERSION}"
