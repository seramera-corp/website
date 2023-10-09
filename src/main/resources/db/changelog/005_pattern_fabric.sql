create table pattern_fabric (
id serial primary key,
pattern_id int references pattern(id) not null,
fabric_type_id int references fabric_type(id) not null,
fabric_length float
)

