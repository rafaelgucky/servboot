package net.servboot.service;

import net.servboot.models.Person;
import net.servboot.server.ServerManager;

public class PersonService {
    public PersonService(){

    }

    public Person getPerson() {
        return new Person("João", 19, "Doe");
    }
}
