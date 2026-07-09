package net.servboot.test;

import net.servboot.sets.DataSet;

public class PersonService {
    private final DataSet<Person> dataSet = new DataSet<>(Person.class);

    public Person find(int id){
        return dataSet.find(id, true);
    }

    public Person findByIndex(int index){
        return dataSet.get(index);
    }

    public Person[] findAll() throws InterruptedException {
        return dataSet.refresh(true).toArray();
    }

    public int count() throws InterruptedException {
        return dataSet.count();
    }

    public boolean add(Person person){
        dataSet.add(person);
        return dataSet.persist() > 0;
    }

    public boolean update(Person person){
        dataSet.find(person.id, false);
        dataSet.update(person);
        return dataSet.persist() > 0;
    }
}
