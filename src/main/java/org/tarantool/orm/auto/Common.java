package org.tarantool.orm.auto;

import javax.lang.model.type.TypeKind;

final class Common {
    static public final String PACKAGE_NAME = "org.tarantool.orm.generated";

    static public String capitalize(String val) {
        return val.substring(0, 1).toUpperCase() + val.substring(1).toLowerCase();
    }

    static public boolean isNumber(TypeKind kind) {
        switch(kind) {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return true;
            default:
                return false;
        }
    }
}
