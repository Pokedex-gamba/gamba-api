drop database if exists pokemon_gamba;

create database pokemon_gamba;

use pokemon_gamba;

create table gamba_history
(
    id binary(16) unique primary key not null,
    user_id varchar(128) not null,
    pokemon_name varchar(64) not null,
    date date not null
)
