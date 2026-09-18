package net.servboot.orm.context;

public class EntityHolder <T> {
    private final T entity;
    private EntityState entityState;

    public EntityHolder(T entity, EntityState entityState) {
        this(entity);
        this.entityState = entityState;
    }

    public EntityHolder(T entity) {
        this.entity = entity;
    }

    public T getEntity() {
        return entity;
    }

    public EntityState getEntityState() {
        return entityState;
    }

    public void setEntityState(EntityState entityState) {
        this.entityState = entityState;
    }
}
