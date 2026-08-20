package net.servboot.test;

import net.servboot.annotations.*;
import net.servboot.annotations.enums.EntityLoad;
import net.servboot.dependency.DependencyInjectionContainer;

import java.util.List;
import java.util.Objects;

@Table(value = "person", schema = "eventer")
public class Person extends Entity {

    @Key(value = "id")
    public int id;
    public String name;

    @Column(load = EntityLoad.EAGER)
    public String lastName;
    public Integer age;

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

    public static PersonService getService() {
        if (service == null) {
            service = Objects.requireNonNullElse(DependencyInjectionContainer.getApplicationScoped(PersonService.class), new PersonService());
        }

        return (PersonService) service;
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
