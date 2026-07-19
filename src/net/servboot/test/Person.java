package net.servboot.test;

import net.servboot.annotations.Column;
import net.servboot.annotations.ForeignKey;
import net.servboot.annotations.Key;
import net.servboot.annotations.Table;
import net.servboot.annotations.enums.EntityLoad;

@Table("person")
public class Person {
    @Key(value = "id", increment = true)
    public int id;
    public String name;

    @Column(load = EntityLoad.EAGER)
    public String lastName;
    public Integer age;

    @ForeignKey(entity = Pet.class)
    public Pet pet;

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

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                ", pet=" + pet +
                '}';
    }
}
