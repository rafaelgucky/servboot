package net.servboot.sets.enums;

public enum EntityState {
    CREATED(0),
    UPDATED(1),
    DELETED(2),
    LOADED(3);

    private int code;

    EntityState(int code) {
        this.code = code;
    }
}
