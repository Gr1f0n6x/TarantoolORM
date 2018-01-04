package org.tarantool.orm.common.operation.result;

import java.util.List;

/**
 * Created by GrIfOn on 03.01.2018.
 */
public interface TarantoolResultSet<T> {
    List<T> get();
}
