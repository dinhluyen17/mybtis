create database if not exists batisdemo;
create table if not exists employees
(
    id integer not null,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    email_address varchar(255) not null,
    primary key(id)
);