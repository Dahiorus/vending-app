#!/bin/bash

usage="$(basename "$0") [options] -- run vending-app for development

where:
    -h  show this help text
    -d  activate remote debug mode for the backend (on port 5005)
    -p  purge expired refresh tokens then exit, instead of running the app
        (equivalent to: ./gradlew :backend:infrastructure:bootRun --args='--spring.profiles.active=dev --purge-expired-refresh-tokens')
    -a  create default admin user for development environment then exit, instead of running the app
        (equivalent to: ./gradlew :backend:infrastructure:bootRun --args='--spring.profiles.active=dev --create-dev-environment-admin')"

DEBUG_OPT=""
PURGE_REFRESH_TOKENS=""
CREATE_DEV_ENVIRONMENT_ADMIN=""
while getopts "hdpa" OPTION; do
  case ${OPTION} in
  h)
    echo "$usage"
    exit
    ;;
  d)
    DEBUG_OPT=" --debug-jvm"
    ;;
  p)
    PURGE_REFRESH_TOKENS="true"
    ;;
  a)
    CREATE_DEV_ENVIRONMENT_ADMIN="true"
    ;;
  *)
    echo "$usage"
    exit
    ;;
  esac
done

if [ -n "$PURGE_REFRESH_TOKENS" ]; then
  ./gradlew :backend:infrastructure:bootRun --args='--spring.profiles.active=dev --purge-expired-refresh-tokens'
  exit
fi

if [ -n "$CREATE_DEV_ENVIRONMENT_ADMIN" ]; then
  ./gradlew :backend:infrastructure:bootRun --args='--spring.profiles.active=dev --create-dev-environment-admin'
  exit
fi

cleanup() {
  killport 4200
  killport 8080
  exit
}

killport() {
  if [ "$(uname)" == "Darwin" ]; then
    lsof -P | grep ":$1" | awk '{print $2}' | xargs kill -9
  else
    fuser --kill $1/tcp > /dev/null 2>&1
  fi
}

trap cleanup INT TERM ERR
trap "kill 0" EXIT

# NB: contrairement à kisscool, ce dépôt ne fournit pas de docker-compose
# pour la base de données. PostgreSQL doit déjà tourner en local
# (jdbc:postgresql://localhost:5432/vending-app, voir
# backend/infrastructure/src/main/resources/application-dev.properties).

# Frontend (port 4200, proxy /api -> backend:8080)
(cd frontend && npm start) &

# Backend (port 8080, profil dev)
./gradlew :backend:infrastructure:bootRun --args='--spring.profiles.active=dev'${DEBUG_OPT} &

wait
