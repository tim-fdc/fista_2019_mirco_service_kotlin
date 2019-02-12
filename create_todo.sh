#!/usr/bin/env bash

set -e

USER_ID=`curl -X GET http://localhost:8081/users/ | jq .[0].id | sed "s/\"//g"`
TODO_ID=`curl -X PUT "http://localhost:8082/todos/" -d "{\"assignee\":\"${USER_ID}\", \"description\":\"test todo\"}" \
-H "accept: application/json;charset=utf-8" \
-H "Content-Type: application/json"`

echo "Created Todo with ID ${TODO_ID} for user with ID ${USER_ID}"
