package net.servboot.models;

public class Person {
    public String name;
    public int age;
    public String lastName;

    public Person() { }

    public Person(String name, int age, String lastName) {
        this.name = name;
        this.age = age;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "Person{" + "name=" + name + ", age=" + age + ", lastName=" + lastName + '}';
    }
}
