package net.servboot.sets.async;

import java.lang.reflect.Field;

public class FindEntityAsync {
    private Field field;
    private Object father;
    private Object value;

    public void setField(Field field) {
        this.field = field;
    }

    public void setFather(Object father) {
        this.father = father;
        fillFatherField();
    }

    public void setValue(Object value) {
        this.value = value;
        fillFatherField();
    }

    private void fillFatherField(){
        try{
            if(field != null && father != null && value != null){
                field.setAccessible(true);
                field.set(father, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
