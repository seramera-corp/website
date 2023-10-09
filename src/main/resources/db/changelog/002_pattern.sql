create table pattern (
id serial primary key,
name varchar,
publisher varchar,
published_in varchar,
publishing_date date,
difficulty varchar,
community_rating_difficulty float,
img_url varchar DEFAULT 'https://drive.google.com/file/d/1MIcOLOdOOQJ4pQ9eBG_yhuatQzXzG98R/preview',
community_rating float
)