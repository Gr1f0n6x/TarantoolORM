package org.tarantool.orm.common.type;

/**
 * Created by GrIfOn on 29.12.2017.
 */
public enum CollationType {
    BINARY("binary"),
    UNICODE("unicode"),
    UNICODE_CI("unicode_ci");

    private String name;

    CollationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
