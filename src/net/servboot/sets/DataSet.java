package net.servboot.sets;

import net.servboot.database.ConnectionManager;
import net.servboot.database.Join;
import net.servboot.sets.async.FindEntityAsync;
import net.servboot.sets.enums.EntityState;
import net.servboot.utils.reflection.InstantiationUtils;
import net.servboot.utils.reflection.ReflectionUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class DataSet<T> implements Iterable<T> {
    private int size = 0;
    private Connection connection;
    private final Class<T> clazz;
    private Node<Entity<T>> root;
    private Set<Field> fields;
    private final Set<Field> eagerFields;
    private final Set<Field> keys;
    private final Set<Field> foreings;
    private List<T> buffer = new LinkedList<>();

    public DataSet(Class<T> clazz, Connection connection, boolean findAll){
        this(clazz);
        this.connection = connection;
        if(findAll){
            findAll();
        }
    }

    public DataSet(Class<T> clazz, boolean findAll){
        this(clazz);
        if(findAll){
            findAll();
        }
    }

    public DataSet(Class<T> clazz) {
        this.clazz = clazz;
        this.fields = ReflectionUtils.getAllFields(clazz);
        this.keys = ReflectionUtils.getKeys(clazz);
        this.eagerFields = ReflectionUtils.getEagerFields(clazz);
        this.foreings = ReflectionUtils.getForeignFields(clazz);
        try{
            this.connection = ConnectionManager.getConnection(Thread.currentThread().getName());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        if(size == 0){
            return this.find(0, 1, 0, false) != null;
        }
        return false;
    }

    public boolean contains(Object o) {
        if(!o.getClass().equals(clazz)) {
            return false;
        } else {
            return this.find(this.keys, ReflectionUtils.getValues(o, this.keys), false) != null;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int offset = 0;
            private int count = 0;

            @Override
            public boolean hasNext() {
                if(count == 0){
                    find(this.offset++, 50, 2, true);
                } else if(this.count == 50){
                    count = 0;
                }

                return !buffer.isEmpty();
            }

            @Override
            public T next() {
                return buffer.get(this.count++);
            }
        };
    }

    public T[] toArray() throws InterruptedException{
        T[] array = (T[]) Array.newInstance(this.clazz, this.count());
        int count = 0;
        try {
            for(T entity : this){
                Array.set(array, count++, entity);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Current count: " + count);
        }

        return array;
    }

    public boolean add(T t) {
        try{
            if(root == null) {
                root = new Node<>(new Entity<>(t, EntityState.CREATED));
            } else {
                Node<Entity<T>> node = getLastNode();
                if(node != null) {
                    node.setNext(new Node<>(new Entity<>(t, EntityState.CREATED)));
                }
            }
            this.size++;
            return true;
        } catch(Exception e){
            return false;
        }
    }

    public boolean remove(Object o) {
        if(!contains(o)) { return false; }
        Node<Entity<T>> node = getNode(clazz.cast(o));

        if(node == null){
            this.add((T) o);
            this.getLastNode().getElement().setState(EntityState.DELETED);
        } else {
            node.getElement().setState(EntityState.DELETED);
        }

        return true;
    }

    public boolean containsAll(Collection<?> c) {
        for(Object element : c){
            if(!contains(element)){ return false; }
        }
        return true;
    }








    public boolean addAll(Collection<? extends T> c) {
        for(T element : c){
            add(element);
        }
        return true;
    }

    public boolean removeAll(Collection<?> c) {
        boolean removedAll = true;

        for(Object element : c){
            if(!remove(element)) removedAll = false;
        }
        return removedAll;
    }

    // ============ public methods =============== //

    public int count() throws InterruptedException {
        try(
            Statement stmt = ConnectionManager.getConnection(Thread.currentThread().getName()).createStatement();
        ) {
            Join join = this.getJoinSql();
            join.setSelect(List.of("count(1)"));
            ResultSet rs = stmt.executeQuery(join.generateSql());
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        return 0;
    }

    public boolean update(T newEntity){
        Node<Entity<T>> node = getNode(newEntity);
        if(node == null) return false;
        node.getElement().setEntity(newEntity);
        node.getElement().setState(EntityState.UPDATED);
        return true;
    }

    public T get(int index) {
        Node<Entity<T>> node = getNode(index);
        if(node == null) {
            T entity = find(index - 1, 1, 1, true);
            return entity;
        }
        return getNode(index).getElement().getEntity();
    }

    /**
     * Define the fields aren't queried on DataBase
     * @param propertyName Name of property
     * @return The current DataSet
     */
    public DataSet<T> mapRemove(String... propertyName) {
        for(String property : propertyName){
            fields.removeIf(f -> f.getName().equalsIgnoreCase(property));
        }
        return this;
    }

    /**
     * Define the fields are queried on DataBase
     * @param propertyName Name of property
     * @return The current DataSet
     */
    public DataSet<T> mapSelect(String... propertyName) {
        this.fields = this.fields.stream()
                .filter(field ->
                        Arrays.stream(propertyName).anyMatch(p -> p.equalsIgnoreCase(ReflectionUtils.getDbFieldName(field))))
                .collect(Collectors.toSet());
        return this;
    }

    /**
     * Persist the current change on DataSet
     * @return int Affected rows
     */
    public int persist() {
        int affectedRows = 0;

        try {
            for (int i = 0; i < this.size(); i++) {
                Node<Entity<T>> node = this.getNode(i);
                int tempRows;

                switch (node.getElement().getState()) {
                    case EntityState.CREATED:
                        Map<Field, Object> values = ReflectionUtils.getFieldsValue(node.getElement().getEntity(), this.fields);

                        for (Field field : this.foreings) {
                            if(field.get(node.getElement().getEntity()) == null) continue;
                            DataSet dataSet = new DataSet<>(field.getType());
                            dataSet.add(field.getType().cast(field.get(node.getElement().getEntity())));
                            dataSet.persist();
                            Field foreignField = ReflectionUtils.getForeignField(field.getType(), ReflectionUtils.getForeignFieldName(field));
                            Map<Field, Object> key = ReflectionUtils.getFieldsValue(field.get(node.getElement().getEntity()), Set.of(foreignField));
                            values.put(field, key.get(foreignField));
                        }
                        int key;
                        if ((key = this.insert(values)) > 0) {
                            Field field = this.keys.stream().filter(ReflectionUtils::isIncrement).findAny().orElseGet(() -> null);
                            if (field != null) {
                                ReflectionUtils.setField(node.getElement().getEntity(), field, key);
                            }

                            node.getElement().setState(EntityState.LOADED);
                            affectedRows++;
                        }
                        break;
                    case EntityState.UPDATED:
                        tempRows = updateEntity(node.getElement().getEntity());
                        if (tempRows > 0) {
                            affectedRows += tempRows;
                        }
                        break;
                    case EntityState.DELETED:
                        String condition = this.getWhereSql(this.keys, ReflectionUtils.getValues(node.getElement().getEntity(), this.keys));
                        if ((tempRows = this.delete(condition)) > 0) {
                            if (i == 0) {
                                if (size > 1) {
                                    this.root = getNode(1);
                                } else {
                                    this.root = null;
                                }
                            } else {
                                node = getNode(i - 1);
                            }

                            affectedRows += tempRows;
                            this.size--;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return affectedRows;
    }

    public <K> T find(K key, boolean findJoins){
        return find(this.keys, List.of(key), findJoins);
    }

    public T find(boolean findJoins, Object... keys){
        return find(this.keys, List.of(keys), findJoins);
    }

    public T find(List<Object> values, boolean findJoins){
        return find(this.fields, values, findJoins);
    }

    public T find(Set<Field> fields, List<Object> values){
        return find(fields, values, true);
    }

    public T find(Set<Field> fields, List<Object> values, boolean findJoins){
        T entity = null;
        try (
            Statement stmt = ConnectionManager.getConnection(Thread.currentThread().getName()).createStatement();
        ){
            entity = this.clazz.getDeclaredConstructor().newInstance();

            Join join = getJoinSql();
            join.addCondition(this.getWhereSql(fields, values).replace("WHERE", ""));

            ResultSet rs = stmt.executeQuery(join.generateSql());
            Map<Class<?>, DataSet<?>> foreign = new LinkedHashMap<>();

            while(rs.next()){
                this.setValues(entity, rs, foreign, findJoins);
                this.addFromBd(entity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return entity;
    }

    public Object findField(Field field, Set<Field> keys, List<Object> values){
        try (
            Statement stmt = ConnectionManager.getConnection(Thread.currentThread().getName()).createStatement();
        ){
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append(this.getSelectSql(Set.of(field)));
            sql.append(" FROM ");
            sql.append(ReflectionUtils.getTableName(clazz));
            sql.append(this.getWhereSql(keys, values));
            sql.append(" LIMIT ").append(1).append(";");

            ResultSet rs = stmt.executeQuery(sql.toString());

            if(rs.next()){
                return rs.getObject(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public DataSet<T> refresh(boolean findJoins){
        this.findAll(findJoins);
        return this;
    }

    // ============== private methods ============== //

    private T find(int offset, int limit, int addInMemoryMode, boolean findJoins) {
        T entity = null;

        this.buffer.clear();

        try(
            Statement stmt = ConnectionManager.getConnection(Thread.currentThread().getName()).createStatement();
        ) {
            entity = this.clazz.getDeclaredConstructor().newInstance();
            Join join = this.getJoinSql();
            join.addOrder(getOrderBySql().replace("ORDER BY", ""));
            join.setOffset(offset);
            join.setLimit(limit);

            ResultSet rs = stmt.executeQuery(join.generateSql());
            Map<Class<?>, DataSet<?>> foreign = new LinkedHashMap<>();

            while(rs.next()){
                this.setValues(entity, rs, foreign, findJoins);
                if(addInMemoryMode == 1){
                    this.addFromBd(entity);
                } else if(addInMemoryMode == 2){
                    this.buffer.add(entity);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return entity;
    }

    private void findAll(){
        findAll(false);
    }

    private void findAll(boolean findJoins) {
        Map<Class<?>, DataSet<?>> foreign = new LinkedHashMap<>();
        this.size = 0;
        this.root = null;

        try (
            Statement stmt = ConnectionManager.getConnection(Thread.currentThread().getName()).createStatement();
        ) {
            Join join = getJoinSql();
            join.addOrder(this.getOrderBySql().replace("ORDER BY", ""));

            ResultSet rs = stmt.executeQuery(join.generateSql());

            while (rs.next()) {
                //System.out.println(Thread.currentThread().getName());
                T entity = this.clazz.getDeclaredConstructor().newInstance();
                if(this.setValues(entity, rs, foreign,  findJoins)){
                    this.addFromBd(entity);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean setValues(T entity, ResultSet rs, Map<Class<?>, DataSet<?>> foreign, boolean findJoins) throws SQLException {
        for(Field field : this.fields){
            String fieldName = ReflectionUtils.getDbFieldName(field);

            if(ReflectionUtils.isForeign(field)){
                if(findJoins && !ReflectionUtils.isTransient(field)){
                    if(ReflectionUtils.isEager(field)){
                        Map<String, Object> values = new LinkedHashMap<>();
                        for(Field f : ReflectionUtils.getAllFields(field.getType())){
                            values.put(f.getName(), rs.getObject(ReflectionUtils.getTableName(field.getType()) + "." + ReflectionUtils.getDbFieldName(f)));
                        }

                        Set<Field> foreignEntityKeys = new LinkedHashSet<>();
                        foreignEntityKeys.add(ReflectionUtils.getField(ReflectionUtils.getForeignClazz(field), ReflectionUtils.getForeignFieldName(field)));
                        ReflectionUtils.setField(entity, field, InstantiationUtils.instantiate(field.getType(), ReflectionUtils.getAllFields(field.getType()).stream().toList(), values));
                    } else {
                        FindEntityAsync findEntityAsync = new FindEntityAsync();
                        findEntityAsync.setField(field);
                        findEntityAsync.setFather(entity);
                        Object key = rs.getObject(fieldName);

                        new Thread(() -> {
                            try{
                                DataSet<?> foreignDataSet;

                                if(foreign.containsKey(field.getType())){
                                    foreignDataSet = foreign.get(field.getType());
                                } else {
                                    foreignDataSet = new DataSet<>(field.getType(), false);
                                    foreign.put(field.getType(), foreignDataSet);
                                }

                                Set<Field> foreignEntityKeys = new LinkedHashSet<>();
                                foreignEntityKeys.add(ReflectionUtils.getField(ReflectionUtils.getForeignClazz(field), ReflectionUtils.getForeignFieldName(field)));
                                findEntityAsync.setValue(foreignDataSet.find(foreignEntityKeys, List.of(key)));
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }).start();
                    }
                }
            } else {
                if(ReflectionUtils.isEager(field)){
                    ReflectionUtils.setField(entity, field, rs.getObject(ReflectionUtils.getTableName(this.clazz) + "." + fieldName));
                } else {
                    FindEntityAsync findEntityAsync = new FindEntityAsync();
                    findEntityAsync.setField(field);
                    findEntityAsync.setFather(entity);
                    new Thread(() -> {
                        findEntityAsync.setValue(this.findField(field, this.keys, ReflectionUtils.getValues(entity, this.keys)));
                    }).start();
                }
            }
        }

        return true;
    }

    private void addFromBd(T t) {
        try{
            if(root == null) {
                root = new Node<>(new Entity<>(t, EntityState.LOADED));
            } else {
                Node<Entity<T>> node = getLastNode();
                if(node != null) {
                    node.setNext(new Node<>(new Entity<>(t, EntityState.LOADED)));
                }
            }
            this.size++;
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private int insert(Map<Field, Object> values){
        StringBuilder columns = new StringBuilder("INSERT INTO ");
        StringBuilder insert = new StringBuilder(" VALUES ( ");
        columns.append(ReflectionUtils.getTableName(clazz));
        columns.append(" (");

        int count = 0;
        for(Map.Entry<Field, Object> entry : values.entrySet()){
            columns.append(ReflectionUtils.getDbFieldName(entry.getKey()));
            if(entry.getValue() == null){
                insert.append("null");
            } else {
                insert.append("'");
                insert.append(entry.getValue());
                insert.append("'");
            }

            if(count++ != values.size() - 1){
                columns.append(", ");
                insert.append(", ");
            }
        }

        columns.append(" ) ");
        insert.append(" ); ");

        return this.executeSqlKey(columns + insert.toString());
    }

    private int updateEntity(T newEntity){
        try {
            StringBuilder sql = new StringBuilder("UPDATE ");
            sql.append(ReflectionUtils.getTableName(clazz));
            sql.append(" SET ");

            Set<Field> fs = ReflectionUtils.getAllFields(clazz);
            List<Object> values = ReflectionUtils.getValues(newEntity, fs);
            int count = 0;
            for (Field field : fs) {
                if(field.get(newEntity) == null){
                    sql.append(ReflectionUtils.getTableName(clazz)).append(".").append(field.getName()).append(" = ").append("null");
                } else {
                    if (ReflectionUtils.isForeign(field)) {
                        Field foreignField = ReflectionUtils.getForeignField(field.getType(), ReflectionUtils.getForeignFieldName(field));
                        Map<Field, Object> key = ReflectionUtils.getFieldsValue(field.get(newEntity), Set.of(foreignField));
                        values.add(count, key.get(foreignField));
                    }
                    sql.append(ReflectionUtils.getTableName(clazz)).append(".").append(field.getName()).append(" = ").append("'").append(values.get(count)).append("'");
                    if (count++ < values.size() - 1) {
                        sql.append(", ");
                    }
                }
            }

            sql.append(getWhereSql(this.keys, ReflectionUtils.getValues(newEntity, this.keys)));

            return executeSqlCount(sql.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int delete(String condition){
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ");
        sql.append(ReflectionUtils.getTableName(clazz));
        sql.append(" ");
        sql.append(condition);
        return this.executeSqlCount(sql.toString());
    }

    private boolean compareEntities(T entity1,  T entity2){
        List<Object> values1 = ReflectionUtils.getValues(entity1, this.keys);
        List<Object> values2 = ReflectionUtils.getValues(entity2, this.keys);

        for(int i = 0; i < values1.size(); i++){
            if(!values1.get(i).equals(values2.get(i))){
                return false;
            }
        }

        return true;
    }

    // ============== SQL =================== //

    private Statement executeSql(String sql){
        try {
            Statement stmt = ConnectionManager.getConnection(Thread.currentThread().getName()).createStatement();
            System.out.println(sql);
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            return stmt;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private int executeSqlCount(String sql){
        try(
            Statement stmt = executeSql(sql);
        ) {
            if(stmt == null){ throw  new SQLException("Statement is null"); }
            return stmt.getUpdateCount();
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private int executeSqlKey(String sql){
        try(
            Statement stmt = executeSql(sql);
        ) {
            if(stmt == null){ throw  new SQLException("Statement is null"); }
            ResultSet rs = stmt.getGeneratedKeys();
            if(rs != null && rs.next()){
                return rs.getInt(1);
            }
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }

        return -1;
    }

    private String getSelectSql(Set<Field> fields){
        StringBuilder select = new StringBuilder();

        int count = 0;
        for(Field f :  fields){
            select.append(ReflectionUtils.getTableName(clazz));
            select.append(".");
            select.append(f.getName());
            if(++count < fields.size()){
                select.append(", ");
            }

            select.append("\n");
        }

        return select.toString();
    }

    private String getWhereSql(Set<Field> fields, List<Object> values){
        StringBuilder condition;

        if(fields.isEmpty() || values.isEmpty()){
            return "";
        } else if(fields.size() != values.size()){
            throw new RuntimeException("Incompatible values");
        }

        condition = new StringBuilder();

        int count = 0;
        for(Field field : fields){
            if(count == 0){
                condition.append("\nWHERE ");
            } else{
                condition.append("\nAND ");
            }

            condition.append(ReflectionUtils.getTableName(clazz));
            condition.append(".");
            condition.append(field.getName());
            condition.append(" = ");
            condition.append("'");
            condition.append(values.get(count++));
            condition.append("'");
        }

        return condition.toString();
    }

    private Join getJoinSql(){
        Join join = new Join(ReflectionUtils.getTableName(clazz));

        for(Field field : this.eagerFields){
            join.addSelect(ReflectionUtils.getTableName(this.clazz) + "." + ReflectionUtils.getDbFieldName(field));
        }

        for(Field foreignField : ReflectionUtils.getForeignFields(this.clazz)){
            Class<?> foreignClass = ReflectionUtils.getForeignClazz(foreignField);
            for(Field field : ReflectionUtils.getAllFields(foreignClass)){
                join.addSelect(ReflectionUtils.getTableName(foreignClass) + "." + ReflectionUtils.getDbFieldName(field));
            }
        }



        for(Field field : this.foreings){
            StringBuilder joinSql = new StringBuilder();
            String foreignTableName = ReflectionUtils.getTableName(field.getType());

            joinSql.append(ReflectionUtils.isNotNull(field) ? " JOIN " : " LEFT JOIN ").append(foreignTableName).append(" ON ");
            joinSql.append(ReflectionUtils.getTableName(this.clazz)).append(".").append(ReflectionUtils.getDbFieldName(field));
            joinSql.append(" = ");
            joinSql.append(foreignTableName).append(".").append(ReflectionUtils.getDbFieldName(ReflectionUtils.getForeignField(field.getType(), ReflectionUtils.getForeignFieldName(field))));
            join.addJoin(joinSql.toString());
        }

        return join;
    }

    private String getOrderBySql(){
        StringBuilder orderBy = new StringBuilder();

        int count = 0;
        for(Field field : this.keys){
            orderBy.append(ReflectionUtils.getTableName(this.clazz)).append(".").append(ReflectionUtils.getDbFieldName(field));
            if(count++ < this.keys.size() - 1){
                orderBy.append(", ");
            }
        }

        return orderBy.toString();
    }


    // ==================== Node ================= //

    private Node<Entity<T>> getNode(int index){
        if(root == null) { return null; }

        Node<Entity<T>> node = root;

        for(int i = 0; i < index; i++){
            if((node = node.getNext()) == null){
                return null;
//                throw new IndexOutOfBoundsException("Index out of bounds");
            }
        }

        return node;
    }

    private Node<Entity<T>> getNode(T object){
        if(root == null || object == null){ return null; }

        Node<Entity<T>> node = root;

        while(node != null){
            if(this.compareEntities(node.getElement().getEntity(), object)){
                return node;
            }
            node = node.getNext();
        }

        return null;
    }

    private Node<Entity<T>> getPrevNode(T object){
        if(root == null || object == null || root.getElement().equals(object)){ return null; }

        Node<Entity<T>> prevNode = root;
        Node<Entity<T>> node = root.getNext();

        while(node != null){
            if(node.getElement().getEntity().equals(object)){
                return prevNode;
            }

            prevNode = node;
            node = node.getNext();
        }

        return null;
    }

    private Node<Entity<T>> getLastNode(){
        if(this.root == null) return null;
        Node<Entity<T>> node = this.root;

        while(node.getNext() != null){
            node = node.getNext();
        }

        return node;
    }
}