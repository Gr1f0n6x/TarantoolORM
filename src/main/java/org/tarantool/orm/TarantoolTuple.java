package org.tarantool.orm;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by GrIfOn on 20.12.2017.
 */
abstract public class TarantoolTuple {
    public TarantoolTuple() {
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

    final public List<?> getValues() {
        return this
                .getFields()
                .stream()
                .map(TarantoolField::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "org.tarantool.orm.TarantoolTuple{" + this.getFields().toString() +  "}";
    }
}
