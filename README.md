# Another Todo App
This a simple todo app written in Kotlin, [armeria](https://armeria.dev/), prostgres, kafka. 
the app also uses prometheus for metrics. 

## [gRPC Service](app)

This app implements CRUD gRPC (Create, Delete, Update, Get)
grpc services.

## [Http client](client)

the app implements an http controller that calls the gRPC
services and maps the grpc response to json. 

## [Kafka consumer](kafkaconsumerapp)

the app implements a kafka client consumer that consumes 
grpc service messages from the `todos` topic. this consumer only logs info.

## Getting started

### Without docker

1. Install java 17+ 
2. install postgres and use `app/sql/create_tables.sql`to create the tables
3. install gradle 
4. install kafka and zookeeper
see `app/infra/build/commands.sh` for kafka and zookeeper configs as well as how to create a topic
5. run `./gradlew build`
6. In your IDE (intellij) start the app or run `./gradlew run`

### With docker and docker compose

1. run `'./grdlew build`
2. run `docker compose build --no-cache`
3. run `docker compose up`

to stop the containers run `docker compose down`. If you need to clean up the db
run `docker compose down --volunes`



