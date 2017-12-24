package org.tarantool.orm;

import org.tarantool.orm.annotation.IndexField;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by GrIfOn on 20.12.2017.
 */
abstract public class TarantoolTuple {
    private final int fieldCount;

    public TarantoolTuple() {
        fieldCount = this.getFields().size();
    }

    public int getFieldCount() {
        return fieldCount;
    }

    final public List<?> getValues() {
        return this
                .getFields()
                .stream()
                .map(TarantoolField::getValue)
                .collect(Collectors.toList());
    }

    final public List<?> getIndexValues(String indexName) {
        return this
                .getIndexFields(indexName)
                .stream()
                .map(TarantoolField::getValue)
                .collect(Collectors.toList());
    }

    final public Object[] getValuesForUpdate() {
        List<TarantoolField> fields = this.getFields();

        return IntStream
                .range(0, this.fieldCount)
                .mapToObj(i -> Arrays.asList("=", i, fields.get(i).getValue()))
                .collect(Collectors.toList()).toArray();
    }

    private List<TarantoolField> getFields() {
        return Stream.of(this.getClass().getDeclaredFields())
                .filter(x -> {
                    x.setAccessible(true);
                    return x.getType().equals(TarantoolField.class);
                })
                .map(x -> {
                    try {
                        return (TarantoolField) x.get(this);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    private List<TarantoolField> getIndexFields(String indexName) {
        return Stream.of(this.getClass().getDeclaredFields())
                .filter(x -> {
                    x.setAccessible(true);
                    return x.getType().equals(TarantoolField.class) && indexName.equals(x.getAnnotation(IndexField.class).indexName());
                })
                .map(x -> {
                    try {
                        return (TarantoolField) x.get(this);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Tuple{" + this.getFields().toString() +  "}";
    }
}
