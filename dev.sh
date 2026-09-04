#!/bin/bash

usage="$(basename "$0") [options] -- run vending-app for development

where:
    -h  show this help text
    -d  activate remote debug mode for the backend (on port 5005)"

DEBUG_OPT=""
while getopts "hd" OPTION; do
  case ${OPTION} in
  h)
    echo "$usage"
    exit
    ;;
  d)
    DEBUG_OPT=" --debug-jvm"
    ;;
  *)
    echo "$usage"
    exit
    ;;
  esac
done

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
