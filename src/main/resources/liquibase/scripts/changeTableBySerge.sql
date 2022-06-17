-- liquibase formatted sql

-- changeSet serge:1
create table notification_task
(
    id                      serial NOT NULL PRIMARY KEY,
    chat_id                 bigint NOT NULL,
    notification_data       timestamp NOT NULL,
    notification_message    text NOT NULL,
    sent_status             boolean NOT NULL DEFAULT false
);