package net.servboot.test.model;

import net.servboot.annotations.*;
import java.util.List;

@Table(tableName = "tbevent", schema = "eventer")
public class Event {
    @Key
    private Integer id;
    @Column(name = "name", notNull = true)
    private String name;
    @Column(name = "description", notNull = true)
    private String description;
    @OneToOne(targetClass = User.class)
    private User owner;
    @OneToMany(targetClass = Person.class)
    private transient List<Person> people;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }
}
