package net.servboot.orm.context;

public enum EntityState {
    CREATED(1),
    UPDATED(2),
    DELETED(3),
    LOADED(4);

    private final int code;

    EntityState(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
