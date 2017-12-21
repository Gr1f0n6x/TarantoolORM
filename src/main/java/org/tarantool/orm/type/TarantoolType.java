package org.tarantool.orm.type;

/**
 * Created by GrIfOn on 21.12.2017.
 */
public enum TarantoolType {
    UNSIGNED("unsigned"),
    STRING("str"),
    INTEGER("integer"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    ARRAY("array"),
    SCALAR("scalar");

    private final String type;

    TarantoolType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
