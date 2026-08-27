/* table: eventer.person */
drop table if exists eventer.person;

create table eventer.person (
	id bigint not null,
	cpf varchar (20) unique,
	name varchar(80) not null,
	lastname varchar(80) notnull,
	dateofbirth date not null,
	email varchar(255) unique,
	phone varchar(20),
	recorddatetime datetim
	primary key (id)
);

/* table: eventer.user */
create table eventer.user (
	id int null not,
	active bool not null default 0,
	
);

/* table: eventer.errorlogs */
create table if not exists eventer.errorlogs (
	message text not null
);

alter table eventer.errorlogs add column if not exists stacktrace text;