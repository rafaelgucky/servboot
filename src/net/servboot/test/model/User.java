package net.servboot.test.model;

import net.servboot.orm.annotations.Key;
import net.servboot.orm.annotations.OneToOne;
import net.servboot.orm.annotations.Table;

@Table(tableName = "tbuser", schema = "eventer")
public class User {

    @Key
    private Integer id;

    @OneToOne(targetClass = Person.class)
    private Person person;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
