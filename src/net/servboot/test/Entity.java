package net.servboot.test;

public abstract class Entity {
    protected static IService<?> service;

    public static void setService(IService<?> service) {
        Entity.service = service;
    }
}
