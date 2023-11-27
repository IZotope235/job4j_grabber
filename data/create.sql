CREATE TABLE IF NOT EXISTS post (
	id serial primary key,
	title text,
	description text,
	link text UNIQUE,
	created timestamp);