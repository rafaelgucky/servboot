package net.servboot.test;

import net.servboot.annotations.Key;
import net.servboot.annotations.OneToOne;
import net.servboot.annotations.Table;

@Table(value = "tbuser", schema = "eventer")
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
