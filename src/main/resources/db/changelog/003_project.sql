create table project (
id serial primary key,
name varchar,
pattern_id int references pattern(id),
app_user_id int references app_user(id) not null
)