create table category (
id serial primary key,
parent_category_id int references category(id),
category_name varchar
)

