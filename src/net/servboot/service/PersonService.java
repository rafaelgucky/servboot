package net.servboot.service;

import net.servboot.models.Person;
import net.servboot.server.ServerManager;

public class PersonService {
    private BaseService baseService;

    public PersonService(BaseService baseService){
        this.baseService = baseService;
    }

    public Person getPerson() {
        baseService.hello();
        return new Person("João", 19, "Doe");
    }
}
