insert into pattern (name, publisher)
values ('Fancy Shirt', 'Burda');

insert into pattern (name, publisher, rating)
values ('Smooth Pants', 'Mara''s Sewing Recipes', 5);

insert into pattern (name, publisher)
values ('Large Hat', 'Hatmakers Inc.');

insert into project (name, pattern_id, app_user_id)
values (
  'Christoph makes a Shirt',
  (select id from pattern where pattern.name = 'Fancy Shirt'),
  (select id from app_user where app_user.username = 'Christoph'));

insert into project (name, pattern_id, app_user_id)
values (
  'Sarah makes silky smooth pants',
  (select id from pattern where pattern.name = 'Smooth Pants'),
  (select id from app_user where app_user.username = 'Sarah'));

insert into project (name, pattern_id, app_user_id)
values (
  'Mara joins the Silly Hat Club',
  (select id from pattern where pattern.name = 'Large Hat'),
  (select id from app_user where app_user.username = 'Mara'));