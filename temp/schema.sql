create table eventer.person (
	id int not null,
	name varchar(80) not null,
	lastname varchar(80) not null,
	age smallint not null
);

alter table eventer.person add primary key (id);

create table eventer.pet (
	id int not null,
	name varchar(80),
	personId int not null
);

alter table eventer.pet add primary key (id);
alter table eventer.pet add foreign key (personId) references eventer.person (id);
alter table eventer.pet alter column name set not null;

select person.id  as "id", 
 person.name  as "name", 
 person.age  as "age", 
 person.lastname  as "lastName", 
 pet.name  as "Pet.name", 
 pet.id  as "Pet.id" 
 from eventer.person 
left join eventer.pet on  pet.personId = person.id;

insert into eventer.person values (1, 'Rafael', 'Gucky', 19);
insert into eventer.pet values (1, 'Spike', 1);