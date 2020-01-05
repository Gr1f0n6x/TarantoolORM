package org.tarantool.orm.auto;

final class Common {
    static public final String PACKAGE_NAME = "org.tarantool.orm.generated";

    static public String capitalize(String val) {
        return val.substring(0, 1).toUpperCase() + val.substring(1).toLowerCase();
    }
}
