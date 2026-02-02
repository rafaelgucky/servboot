package net.servboot.utils.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class Json {
    private static String jsonTypeName = "instanceType";

    public static void setJsonTypeName(String jsonTypeName) {
        Json.jsonTypeName = jsonTypeName;
    }

    public static String encode(Object obj) throws Exception {
        return encode(obj, Object.class, null);
    }

    public static <T> String encode(List<T> objects) throws Exception {
        return encode(objects.toArray(), Object.class);
    }
    private static <T> String encode(T[] objects, Class<?> superClass) throws IllegalAccessException {
        StringBuilder json = new StringBuilder("[");
        for(int i = 0; i < objects.length; i++){
            StringBuilder inj = new StringBuilder();
            if(!objects.getClass().getComponentType().equals(objects[i].getClass())){
                inj.append("\"");
                inj.append(jsonTypeName);
                inj.append("\": ");
                inj.append("\"");
                inj.append(objects[i].getClass().getName());
                inj.append("\",");
            }
            json.append(encode(objects[i], superClass, inj.toString()));
            if(i != objects.length - 1){
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    public static <T> String encode(T[][] objects, Class<?> superClass) throws IllegalAccessException {
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

    public static <T> String encode(T[][][] objects, Class<?> superClass) throws IllegalAccessException {
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

    private static String encode(Object object, Class<?> superClass, String injectJson) throws IllegalAccessException {
        StringBuilder json = new StringBuilder();
        List<Field> fields = new ArrayList<>(Arrays.asList(object.getClass().getDeclaredFields()));
        Class<?> type = object.getClass().getSuperclass();
        while (type != null && type != Object.class) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        String[] names = fields.stream().map(Field::getName).toArray(String[]::new);
        json.append("{");
        if(injectJson != null && !injectJson.isEmpty()){
            json.append(injectJson);
        }
        for (int j = 0; j < fields.size(); j++) {
            fields.get(j).setAccessible(true);
            Object value = fields.get(j).get(object);
            json.append("\"");
            json.append(names[j]);
            json.append("\": ");
            fields.get(j).setAccessible(true);
            if(value == null){
                json.append("null");
                if(j < fields.size() - 1){
                    json.append(",");
                }
            } else if(fields.get(j).getType() == String.class
                    || fields.get(j).getType() == char.class){
                json.append("\"");
                json.append(fields.get(j).get(object));
                if(j < fields.size() - 1){
                    json.append("\",");
                }
                else{
                    json.append("\"");
                }
            } else if(fields.get(j).getType() == int.class
                    || fields.get(j).getType() == long.class
                    || fields.get(j).getType() == double.class
                    || fields.get(j).getType() == float.class
                    || fields.get(j).getType() == boolean.class
                    || fields.get(j).getType() == byte.class
                    || fields.get(j).getType() == short.class) {
                json.append(fields.get(j).get(object));
                if(j < fields.size() - 1){
                    json.append(",");
                }
            }
            else if((value.getClass().isArray() || value instanceof Collection) && fields.get(j).getType() != superClass){
                int dimension = 0;
                Class<?> t = fields.get(j).getType().getComponentType();
                while(t != null){
                    dimension++;
                    t = t.getComponentType();
                }
                switch (dimension){
                    case 1:
                        json.append(encode((Object[]) value, object.getClass()));
                        if(j < fields.size() - 1){
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
            } else if(value.getClass().isEnum()){
                json.append("\"");
                json.append(((Enum<?>) value).name());
                json.append("\"");
                if(j < fields.size() - 1){
                    json.append(",");
                }
            }
            else if(fields.get(j).getType() != superClass) {
                StringBuilder injectedJson = new StringBuilder();
                if((fields.get(j).get(object).getClass() != Integer.class) && !fields.get(j).get(object).getClass().getSimpleName().equalsIgnoreCase(fields.get(j).getType().getSimpleName())){
                    injectedJson.append("\"");
                    injectedJson.append(jsonTypeName);
                    injectedJson.append("\": ");
                    injectedJson.append("\"");
                    injectedJson.append(fields.get(j).get(object).getClass().getName());
                    injectedJson.append("\",");
                }
                json.append(encode(value, object.getClass(), injectedJson.toString()));
                if(j < fields.size() - 1){
                    json.append(",");
                }
            } else{
                json.append("\"");
                json.append(value.getClass().getSimpleName());
                json.append("\"");
                if(j < fields.size() - 1){
                    json.append(",");
                }
            }

        }
        json.append("}");
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
                value.append(json.charAt(count));
                if (json.charAt(count) == '[') {
                    skipBrackets++;
                    skipQuotes = 0;
                } else if (json.charAt(count) == '{') {
                    skipKeys++;
                    skipQuotes = 0;
                } else if (json.charAt(count) == ']') {
                    skipBrackets--;
                } else if (json.charAt(count) == '}') {
                    skipKeys--;
                } else if(json.charAt(count) == '\"') {
                    skipQuotes--;
                }
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
        if(!value.trim().isEmpty()){
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(obj, value.trim().replace("\"", ""));
            } else if (field.getType().equals(int.class)) {
                field.set(obj, Integer.parseInt(value));
            } else if (field.getType().equals(long.class)) {
                field.set(obj, Long.parseLong(value));
            } else if (field.getType().equals(double.class)) {
                field.set(obj, Double.parseDouble(value));
            } else if (field.getType().equals(float.class)) {
                field.set(obj, Float.parseFloat(value));
            } else if (field.getType().equals(char.class)) {
                field.set(obj, value.charAt(0));
            } else if (field.getType().equals(boolean.class)) {
                field.set(obj, Boolean.parseBoolean(value));
            } else if (field.getType().equals(byte.class)) {
                field.set(obj, Byte.parseByte(value));
            } else if (field.getType().equals(short.class)) {
                field.set(obj, Short.parseShort(value));
            }
        }
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
            } else if(json.charAt(i) != ' ' && json.charAt(i) != '\n'){
                sb.append(json.charAt(i));
            }
        }
        return sb.toString();
    }
}