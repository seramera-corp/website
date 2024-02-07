create table pattern_clothing_type(
    id             serial primary key,
    pattern_id     int references pattern (id)     not null,
    clothing_type_id int references clothing_type (id) not null
)

