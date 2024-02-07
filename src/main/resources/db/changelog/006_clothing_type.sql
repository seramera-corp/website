create table clothing_type (
id serial primary key,
parent_id int references clothing_type(id),
name varchar
)

