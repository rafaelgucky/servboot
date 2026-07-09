package net.servboot.test;

import net.servboot.annotations.Column;
import net.servboot.annotations.Key;
import net.servboot.annotations.Table;

@Table("pet")
public class Pet {
    @Key()
    @Column(name = "petId")
    public int id;
    public String name;

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
