create sequence if not exists global_sequence start 1 increment 1;

create table if not exists city
(
    id        bigint           not null primary key,
    name      varchar          not null,
    latitude  double precision not null,
    longitude double precision
);

create table if not exists distance
(
    id           bigint not null primary key,
    from_city_id bigint references city,
    to_city_id   bigint references city,
    distance     double precision not null
);
