package net.servboot.utils.json;

import net.servboot.orm.ModelIterator;
import net.servboot.utils.reflection.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class Json {
    private static String jsonTypeName = "instanceType";

    public static void setJsonTypeName(String jsonTypeName) {
        Json.jsonTypeName = jsonTypeName;
    }

    public static String encode(Object obj) {
        try {
            if (obj instanceof ModelIterator) {
                return encode((ModelIterator<?>) obj, Object.class);
            } else if(obj instanceof Collection<?>){
                return encode((List<?>) obj);
            } else if(obj.getClass().isArray()){
                Class<?> componentType;
                int dimensions = 1;
                while((componentType = obj.getClass().getComponentType()) != null && componentType.isArray()){
                    dimensions++;
                }

                return switch (dimensions) {
                    case 1 -> encode((Object[]) obj, obj.getClass().getComponentType());
                    case 2 -> encode((Object[][]) obj, obj.getClass().getComponentType());
                    case 3 -> encode((Object[][][]) obj, obj.getClass().getComponentType());
                    default -> "[]";
                };
            } else {
                return encode(obj, Object.class, null);
            }
        } catch (Exception ignored) {
            return "[]";
        }
    }

    public static <T> String encode(List<T> objects) throws Exception {
        return encode(objects.toArray(), Object.class);
    }

    private static <T> String encode(ModelIterator<T> modelIterator, Class<?> superClass)
            throws IllegalAccessException, InvocationTargetException {
        StringBuilder json = new StringBuilder("[");

        while (modelIterator.hasNext()) {
            T obj = modelIterator.next();

            json.append(encode(obj, superClass, ""));
            if(!modelIterator.isLast()){
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static <T> String encode(T[] objects, Class<?> superClass)
            throws IllegalAccessException, InvocationTargetException {
        StringBuilder json = new StringBuilder("[");
        for(int i = 0; i < objects.length; i++){
            json.append(encode(objects[i], superClass, ""));
            if(i != objects.length - 1){
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    public static <T> String encode(T[][] objects, Class<?> superClass)
            throws IllegalAccessException, InvocationTargetException {
        StringBuilder json = new StringBuilder("[");
        for(int i = 0; i < objects.length; i++){
            json.append(encode(objects[i], superClass));
            if(i != objects.length - 1){
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    public static <T> String encode(T[][][] objects, Class<?> superClass)
            throws IllegalAccessException, InvocationTargetException {
        StringBuilder json = new StringBuilder("[");
        for(int i = 0; i < objects.length; i++){
            json.append(encode(objects[i], superClass));
            if(i != objects.length - 1){
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String encode(Object object, Class<?> superClass, String injectJson)
            throws IllegalAccessException, InvocationTargetException {
        StringBuilder json = new StringBuilder();

        if(object instanceof Map || object instanceof Dictionary){
            json.append(encodeMap(object));
        } else if(object.getClass().equals(Character.class)
                ||  object.getClass().equals(String.class)) {
            json.append("\"");
            json.append(object);
            json.append("\"");
        } else if(ReflectionUtils.isPrimitive(object.getClass())) {
            json.append(object);
        } else {
            Set<Field> fields = ReflectionUtils.getAllFields(object.getClass()).stream()
                    .filter(field -> !ReflectionUtils.isStatic(field) && !ReflectionUtils.isTransient(field))
                    .collect(Collectors.toSet());

            String[] names = fields.stream().map(Field::getName).toArray(String[]::new);
            json.append("{");
            if (injectJson != null && !injectJson.isEmpty()) {
                json.append(injectJson);
            }

            int count = 0;
            for (Field field : fields) {
                Object value;
                if (!field.getType().equals(Map.class) && !field.getType().equals(Dictionary.class)) {
                    json.append("\"");
                    json.append(names[count]);
                    json.append("\": ");
                    value = ReflectionUtils.callGetter(object, field.getName());
                } else {
                    value = object;
                }
                field.setAccessible(true);
                if (value == null) {
                    json.append("null");
                    if (count < fields.size() - 1) {
                        json.append(",");
                    }
                } else if (field.getType() == String.class
                        || field.getType() == char.class) {
                    json.append("\"");
                    json.append(field.get(object));
                    if (count < fields.size() - 1) {
                        json.append("\",");
                    } else {
                        json.append("\"");
                    }
                } else if (ReflectionUtils.isPrimitive(field.getType())) {
                    json.append(field.get(object));
                    if (count < fields.size() - 1) {
                        json.append(",");
                    }
                } else if (value instanceof Collection)  {
                    json.append(encode(value));
                } else if ((value.getClass().isArray()) && field.getType() != superClass) {
                    int dimension = 0;
                    Class<?> t = field.getType().getComponentType();
                    while (t != null) {
                        dimension++;
                        t = t.getComponentType();
                    }
                    switch (dimension) {
                        case 1:
                            json.append(encode((Object[]) value, object.getClass()));
                            if (count < fields.size() - 1) {
                                json.append(",");
                            }
                            break;
                        case 2:
                            json.append(encode((Object[][]) value, superClass));
                            break;
                        case 3:
                            json.append(encode((Object[][][]) value, superClass));
                            break;
                        default:
                            json.append(" \"Dimesions (");
                            json.append(dimension);
                            json.append(")\"");
                    }
                } else if (value.getClass().isEnum()) {
                    json.append("\"");
                    json.append(((Enum<?>) value).name());
                    json.append("\"");
                    if (count < fields.size() - 1) {
                        json.append(",");
                    }
                } else if (field.getType() != superClass) {
                    StringBuilder injectedJson = new StringBuilder();
                    if ((field.get(object).getClass() != Integer.class) && !field.get(object).getClass().getSimpleName().equalsIgnoreCase(field.getType().getSimpleName())) {
                        injectedJson.append("\"");
                        injectedJson.append(jsonTypeName);
                        injectedJson.append("\": ");
                        injectedJson.append("\"");
                        injectedJson.append(field.get(object).getClass().getName());
                        injectedJson.append("\",");
                    }
                    json.append(encode(value, object.getClass(), injectedJson.toString()));
                    if (count < fields.size() - 1) {
                        json.append(",");
                    }
                } else {
                    json.append("\"");
                    json.append(value.getClass().getSimpleName());
                    json.append("\"");
                    if (count < fields.size() - 1) {
                        json.append(",");
                    }
                }
                count++;
            }
            json.append("}");
        }
        return json.toString();
    }

    private static String encodeMap(Object object){
        StringBuilder json = new StringBuilder();
        try{
            json.append("{");
            if(object instanceof Map<?, ?> map){
                int length = map.size();
                int count = 0;

                for(Object key  : map.keySet()){
                    json.append("\"");
                    json.append(key.toString());
                    json.append("\": ");
                    Object value = map.get(key);
                    if(value.getClass().equals(Long.class)
                        || value.getClass().equals(Double.class)
                        || value.getClass().equals(Float.class)
                        || value.getClass().equals(Boolean.class)
                        || value.getClass().equals(Byte.class)
                        || value.getClass().equals(Short.class)
                        || value.getClass().equals(Integer.class))
                    {
                        json.append(value);
                    } else if(value.getClass().equals(String.class) || value.getClass().equals(Character.class)){
                        json.append("\"");
                        json.append(value);
                        json.append("\"");
                    } else {
                        json.append(encode(map.get(key), Object.class, ""));
                    }

                    if(++count > 0 && count < length){
                        json.append(",");
                    }
                }
            } else if(object instanceof Dictionary<?,?> dic){
                int length = dic.size();
                int count = 0;
                Enumeration<?> keys = dic.keys();

                while(keys.hasMoreElements()){
                    String key = keys.nextElement().toString();
                    json.append("\"");
                    json.append(key);
                    json.append("\": ");
                    Object value = dic.get(key);
                    if(value.getClass().equals(Long.class)
                            || value.getClass().equals(Double.class)
                            || value.getClass().equals(Float.class)
                            || value.getClass().equals(Boolean.class)
                            || value.getClass().equals(Byte.class)
                            || value.getClass().equals(Short.class)
                            || value.getClass().equals(Integer.class))
                    {
                        json.append(value);
                    } else if(value.getClass().equals(String.class) || value.getClass().equals(Character.class)){
                        json.append("\"");
                        json.append(value);
                        json.append("\"");
                    } else {
                        json.append(encode(dic.get(key), Object.class, ""));
                    }

                    if(++count > 0 && count < length){
                        json.append(",");
                    }
                }
            }
            json.append("}");
        } catch(Exception e){
            e.printStackTrace();
        }
        return json.toString();
    }

    public static <T> T decode(String json, Class<T> c) throws Exception {
        return Json.decode(json, c, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T decode(String json, Class<T> c, Dictionary<String, Class<?>> instanceType) throws Exception {
        json = formatJson(json);
        if(c.isArray()){
            Class<?> type = c.getComponentType();
            List<String> objects = getListOfObjectsFromDimension(json, 0);
            Object t = Array.newInstance(type, objects.size());
            for(int i = 0; i < objects.size(); i++){
                Array.set(t, i, decode(objects.get(i), type, instanceType));
            }
            return (T) t;
        } else {
            return decodeObjPrivate(json, c, instanceType);
        }
    }

    private static <T> List<T> decodePrivate(String json, Class<T> c, Dictionary<String, Class<?>> instanceType) throws Exception {
        List<T> list = new LinkedList<>();
        for(String s : splitObjects(json)){
            T t = decodeObjPrivate(s, c, instanceType);
            list.add(t);
        }
        return list;
    }

    private static LinkedHashMap<Integer, LinkedHashMap<String, String>> splitFields(String json){
        int skipQuotes, skipBrackets, skipKeys;
        int count = 0;
        String k = "";
        int countElements = 0;
        boolean isFirst = true;
        LinkedHashMap<Integer, LinkedHashMap<String, String>> dict = new LinkedHashMap<>();
        LinkedHashMap<String, String> temp = new LinkedHashMap<>();

        while(count < json.length()) {
            skipQuotes = 2;
            skipBrackets = skipKeys = 0;
            StringBuilder key = new StringBuilder();
            StringBuilder value = new StringBuilder();

            // Busca pelo início da chave
            while (count < json.length() && json.charAt(count) != '\"') {
                count++;
            }

            // Busca pela chave
            while(++count < json.length() && json.charAt(count) != '\"'){
                key.append(json.charAt(count));
            }

            // Decide se é um novo elemento (caso seja uma lista)
            if(isFirst){
                k = key.toString();
                isFirst = false;
            } else if(key.toString().equals(k)){
                dict.put(countElements++, temp);
                temp = new LinkedHashMap<>();
            }

            // Busca pelo valor total da propriedade (int, bool, array, object, string)
            count++;

            while (++count < json.length() && ((skipQuotes > 0 && json.charAt(count) != ',') || skipBrackets > 0 || skipKeys > 0)) {
                if (json.charAt(count) == '[') {
                    skipBrackets++;
                    skipQuotes = 0;
                } else if (json.charAt(count) == '{') {
                    skipKeys++;
                    skipQuotes = 0;
                } else if (json.charAt(count) == ']') {
                    skipBrackets--;
                } else if (json.charAt(count) == '}') {
                    if(count == json.length() - 1){
                        count++;
                        break;
                    }
                    skipKeys--;
                } else if(json.charAt(count) == '\"') {
                    skipQuotes--;
                }

                value.append(json.charAt(count));
            }
            if(!key.toString().trim().isEmpty()){
                temp.put(key.toString().trim(), value.toString().trim());
            }
        }
        dict.put(countElements, temp);
        return dict;
    }

    public static boolean isArray(String json){
        return json.trim().charAt(0) == '[';
    }

    public static boolean isObject(String json){
        return json.trim().charAt(0) == '{';
    }

    @SuppressWarnings("unchecked")
    private static <T> T decodeObjPrivate(String json, Class<T> c, Dictionary<String, Class<?>> instanceType) throws Exception {
        List<Field> fields = new LinkedList<>(Arrays.asList(c.getDeclaredFields()));
        LinkedHashMap<Integer, LinkedHashMap<String, String>> map = splitFields(json);
        Iterator<String> it = Arrays.stream(map.get(0).keySet().toArray(String[]::new)).iterator();
        T obj;

        if(map.get(0).keySet().stream().anyMatch(f -> f.equals(jsonTypeName))){
            if(instanceType == null || instanceType.get(map.get(0).get(jsonTypeName).trim().replace("\"", "")) == null){
                throw new RequiredClassRelationNotFoundException("The required class relationship was not found in the map<String, Class<?>>. Use another method overload specifying the relationship.");
            }
            fields.addAll(Arrays.asList(instanceType.get(map.get(0).get(jsonTypeName).trim().replace("\"", "")).getDeclaredFields()));
            obj = (T) instanceType.get(map.get(0).get(jsonTypeName).trim().replace("\"", "")).getDeclaredConstructor().newInstance();
        } else {
            obj = (T) c.getDeclaredConstructor().newInstance();
        }

        while (it.hasNext()){
            String key = it.next();
            if(fields.stream().anyMatch(f -> f.getName().equals(key))){
                Optional<Field> op = fields.stream().filter(f -> f.getName().equals(key)).findFirst();
                if(op.isPresent()){
                    Field field = op.get();
                    field.setAccessible(true);
                    if(field.getType() == Class.class){
                        field.set(obj, Class.forName(map.get(0).get(key).replace("\"", "")));
                    }
                    else if(isObject(map.get(0).get(key))){
                        field.set(obj, decodeObjPrivate(map.get(0).get(key), field.getType(), instanceType));
                    } else if(isArray(map.get(0).get(key))){
                        if(!field.getType().isArray()){
                            field.set(obj, decodePrivate(map.get(0).get(key), field.getType(), instanceType));
                        } else{
                            field.set(obj, decode(map.get(0).get(key), field.getType(), instanceType));
                        }
                    } else if(field.getType().isEnum()) {
                        if (instanceType != null && instanceType.get(key.trim()) != null) {
                            Class<? extends Enum> enumClass = (Class<? extends Enum>) instanceType.get(key.trim());
                            field.set(obj, Enum.valueOf(enumClass, map.get(0).get(key.trim()).trim().replace("\"", "")));
                        }
                    } else{
                        fillValue(obj, field, map.get(0).get(key));
                    }
                }
            }
        }
        return obj;
    }

    private static void fillValue(Object obj, Field field, String value) throws IllegalAccessException {
        if (field.getType() == String.class) {
            value = value.replace("\"", "");
        }

        field.set(obj, ReflectionUtils.convertFromString(value, field.getType()));
    }

    // Split object from a json array
    private static List<String> splitObjects(String json){
        int skip = 1;
        int count = 0;
        List<String> list = new ArrayList<>();
        if(json.charAt(0) == '['){
            while(count < json.length()){
                if(json.charAt(count) == ']'){ break; }
                while(json.charAt(count) != '{') {
                    count++;
                    if(count >= json.length()) break;
                }
                if(count >= json.length()) break;
                StringBuilder sb = new StringBuilder();
                sb.append(json.charAt(count++));
                while(count < json.length() && skip > 0){
                    sb.append(json.charAt(count));
                    if(json.charAt(count) == '{'){
                        skip++;
                    } else if(json.charAt(count) == '}'){
                        skip--;
                    }
                    count++;
                }
                list.add(sb.toString());
                skip = 1;
            }
        }
        return list;
    }

    private static List<String> getListOfObjectsFromDimension(String json, int dimension) throws Exception {
        int skip = 1;
        int count = dimension;
        StringBuilder sb = new StringBuilder();
        List<String> objects = new ArrayList<>();
        if(json.charAt(count + 1) == '{'){
            return splitObjects(json);
        }

        if(json.charAt(count) != '['){
            throw new Exception("Dimension not found on json");
        }
        while(++count < json.length()){
            if(json.charAt(count) == '['){
                skip++;
            } else if(json.charAt(count) == ']'){
                skip--;
                if(skip == 1){
                    sb.append(json.charAt(count++));
                    objects.add(sb.toString());
                    sb = new StringBuilder();
                    continue;
                }
            }
            sb.append(json.charAt(count));
        }
        return objects;
    }

    private static String formatJson(String json){
        json = json.trim();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < json.length(); i++){
            if(json.charAt(i) == '\"'){
                sb.append(json.charAt(i));
                do{
                    sb.append(json.charAt(++i));
                } while(json.charAt(i) != '\"');
            } else if(json.charAt(i) != ' ' && json.charAt(i) != '\n' && json.charAt(i) != '\r'){
                sb.append(json.charAt(i));
            }
        }
        return sb.toString();
    }
}