create table pattern (
id serial primary key,
name varchar,
publisher varchar,
published_in varchar,
publishing_date date,
difficulty varchar,
community_rating_difficulty float,
img_url varchar DEFAULT 'https://drive.google.com/file/d/1O9UV5WgxYKPbIUDqQcggTwLS4t6eNj8p/preview',
community_rating float
)