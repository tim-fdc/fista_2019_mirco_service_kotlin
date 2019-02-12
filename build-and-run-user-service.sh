#!/usr/bin/env bash

# A helper script to quickly build and run the user service.
set -e

export USER_SERVICE_GRPC_PORT=8080
export USER_SERVICE_HTTP_PORT=8081
export TODO_SERVICE_PORT=8082

./gradlew user-service:formatKotlin user-service:clean user-service:build
echo "Successfully created new release of user-service"
java -jar user-service/build/libs/user-service-standalone-0.0.1.jar
