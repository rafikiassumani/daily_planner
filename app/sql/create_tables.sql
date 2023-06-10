
--CREATE DATABASE todo-app;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE todo_status AS ENUM ('CREATED', 'IN_PROGRESS', 'COMPLETED');

CREATE TABLE todo (
    todo_id uuid DEFAULT uuid_generate_v4 (),
    title VARCHAR(255) NOT NULL,
    description VARCHAR NOT NULL,
    author_id VARCHAR(255) NOT NULL,
    status todo_status NOT NULL,
    completed_at TIMESTAMP,
    planned_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_at TIMESTAMP,
    PRIMARY KEY (todo_id)
);