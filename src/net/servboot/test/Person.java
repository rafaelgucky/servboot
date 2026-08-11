package net.servboot.test;

import net.servboot.annotations.*;
import net.servboot.annotations.enums.EntityLoad;

import java.util.List;

@Table("person")
public class Person {
    @Key(value = "id", increment = true)
    public int id;
    public String name;

    @Column(load = EntityLoad.EAGER)
    public String lastName;
    public Integer age;

//    @ForeignKey(entity = Pet.class)

    @OneToMany(targetClass = Pet.class)
    public List<Pet> pets;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                '}';
    }
}
