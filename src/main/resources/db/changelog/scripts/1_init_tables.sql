
create table if not exists city
(
    id        bigint auto_increment  primary key,
    name      varchar(255) not null,
    latitude  double precision not null,
    longitude double precision not null
);

create table if not exists distance
(
    id           bigint auto_increment primary key,
    from_city_id bigint references city,
    to_city_id   bigint references city,
    distance     double precision not null
);
