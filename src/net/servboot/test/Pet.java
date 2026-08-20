package net.servboot.test;

import net.servboot.annotations.Column;
import net.servboot.annotations.Key;
import net.servboot.annotations.Table;

@Table(value = "pet", schema = "eventer")
public class Pet {
    @Key()
    public int id;
    public String name;

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

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
