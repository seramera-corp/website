create table pattern (
id serial primary key,
name varchar,
publisher varchar,
published_in varchar,
rating varchar,
difficulty float,
img_url varchar DEFAULT 'https://drive.google.com/file/d/1MIcOLOdOOQJ4pQ9eBG_yhuatQzXzG98R/preview',
fabric_consumption_metric float,
fabric_consumption_imperial float
)