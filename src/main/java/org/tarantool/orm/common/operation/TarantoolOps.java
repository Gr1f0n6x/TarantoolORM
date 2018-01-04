package org.tarantool.orm.common.operation;

/**
 * Created by GrIfOn on 03.01.2018.
 */

public interface TarantoolOps<T> {
    Object eval(String query);
}
