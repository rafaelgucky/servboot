package net.servboot.models;

public class Person {
    public String name;
    public int age;
    public String lastName;

    @Override
    public String toString() {
        return "Person{" + "name=" + name + ", age=" + age + ", lastName=" + lastName + '}';
    }
}
