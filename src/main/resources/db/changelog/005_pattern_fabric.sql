create table pattern_fabric (
id serial primary key,
pattern_id int references pattern(id) not null,
fabrictype_id int references fabrictype(id) not null,
fabric_length float
)

