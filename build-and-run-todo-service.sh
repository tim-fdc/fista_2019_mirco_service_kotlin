#!/usr/bin/env bash

# A helper script to quickly build and run the user service.
set -e

export TODO_SERVICE_PORT=8082

# Once you completed exercise 2 you should be able to build with tests again
#./gradlew todo-service:formatKotlin todo-service:clean todo-service:build -x test
./gradlew todo-service:formatKotlin todo-service:clean todo-service:build

echo "Successfully created new release of user-service"
java -jar todo-service/build/libs/todo-service-standalone-0.0.1.jar
