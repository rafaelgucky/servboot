package net.servboot.test.model;

import net.servboot.test.service.IService;

public abstract class Entity {
    protected static IService<?> service;

    public static void setService(IService<?> service) {
        Entity.service = service;
    }
}
