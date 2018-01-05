package org.tarantool.orm.entity;

import org.tarantool.orm.common.annotations.IndexField;
import org.tarantool.orm.common.type.OperatorType;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by GrIfOn on 20.12.2017.
 */
abstract public class TarantoolTuple {
    public static Map<Integer, String> fieldMap;

    public TarantoolTuple() {}

    public final int getFieldCount() {
        return fieldMap.size();
    }

    public static <T extends TarantoolTuple> TarantoolTuple build(Class<T> type, List<?> values) {
        TarantoolTuple tuple = null;
        try {
            tuple = type.newInstance();
            tuple.initFromList(values);
        } catch (NoSuchFieldException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return tuple;
    }

    public static <T extends TarantoolTuple> void retrieveFieldMap(Class<T> type) {
        Map<Integer, String> map = new HashMap<>();

        Field[] fields = type.getDeclaredFields();
        for(Field field : fields) {
            if (!field.getType().equals(TarantoolField.class)) {
                continue;
            }

            if (field.getAnnotation(org.tarantool.orm.common.annotations.Field.class) != null) {
                map.put(field.getAnnotation(org.tarantool.orm.common.annotations.Field.class).position(), field.getName());
            }
        }

        fieldMap = map;
    }

    public void initFromList(List<?> values) throws NoSuchFieldException, IllegalAccessException {
        Iterator<?> valIter = values.iterator();

        for(int i = 1; i <= fieldMap.size() && valIter.hasNext(); ++i) {
            String fieldName = fieldMap.getOrDefault(i, "");

            Serializable val = (Serializable) valIter.next();
            Field field = this.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            ((TarantoolField)field.get(this)).setValue(val);
        }
    }

    final public List<?> getValues() {
        List<Object> values = new ArrayList<>();

        for(int i = 1; i <= fieldMap.size(); ++i) {
            String fieldName = fieldMap.getOrDefault(i, "");

            try {
                Field field = this.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                values.add(((TarantoolField)field.get(this)).getValue());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return values;
    }

    final public List<?> getIndexValues(String indexName) {
        List<Object> values = new ArrayList<>();

        for(int i = 1; i <= fieldMap.size(); ++i) {
            String fieldName = fieldMap.getOrDefault(i, "");

            try {
                Field field = this.getClass().getDeclaredField(fieldName);
                if(field.getAnnotation(IndexField.class) != null && indexName.equals(field.getAnnotation(IndexField.class).indexName())) {
                    field.setAccessible(true);
                    values.add(((TarantoolField) field.get(this)).getValue());
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return values;
    }

    final public Object[] getValuesForUpdate() {
        List<List<?>> update = new ArrayList<>();

        try {
            for(Integer key : fieldMap.keySet()) {
                Field field = this.getClass().getDeclaredField(fieldMap.get(key));
                field.setAccessible(true);
                TarantoolField tarantoolField = (TarantoolField) field.get(this);

                update.add(Arrays.asList(OperatorType.ASSIGMENT.getType(), key - 1, tarantoolField.toString()));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return update.toArray();
    }

    @Override
    public String toString() {
        List<String> values = new ArrayList<>();
        try {
            for(Integer key : fieldMap.keySet()) {
                Field field = this.getClass().getDeclaredField(fieldMap.get(key));
                field.setAccessible(true);
                TarantoolField tarantoolField = (TarantoolField) field.get(this);
                values.add(tarantoolField.toString());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return "Tuple{" + values.toString() +  "}";
    }
}
