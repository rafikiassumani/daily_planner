# Another Todo App
todo app written in armeria, prostgres, prometheus and kafka integration

## gRPC Service

This app implements CRUD gRPC (Create, Delete, Update, GetAll todos)
grpc services.

## http client

the app implements an http controller that calls the gRPC
services and map the grpc response to json. 

## kafka consumer

the app implements a kafka client consumer that consumes
grpc service messages from the `todos` topic. the consumer logs info.

## Getting started
### Without docker

1. Install java 17+
2. install gradle
3. install kafka and zookeeper
4. run `./gradlew build`
5. In your IDEA (intellij) start the app
6. install postgres and use `app/sql/create_tables.sql`
#### Kafka config
see `app/infra/build/commands.sh` for kafka and zookeeper configs

### With docker and docker compose

1. run `'./grdlew build`
2. run `docker compose build --no-cache`
3. run `docker compose up`



