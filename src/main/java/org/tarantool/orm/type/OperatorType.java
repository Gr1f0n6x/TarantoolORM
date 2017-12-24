package org.tarantool.orm.type;

/**
 * Created by GrIfOn on 24.12.2017.
 */
public enum OperatorType {
    ADDITION("+"),
    SUBTRACTION("-"),
    OR("|"),
    AND("&"),
    XOR("^"),
    SPLICE(":"),
    INSERTION("!"),
    DELETION("#"),
    ASSIGMENT("=");


    private final String type;

    OperatorType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}