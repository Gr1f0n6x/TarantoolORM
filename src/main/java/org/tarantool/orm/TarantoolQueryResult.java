package org.tarantool.orm;

import java.util.List;

/**
 * Created by GrIfOn on 29.12.2017.
 */
public interface TarantoolQueryResult<T extends TarantoolTuple> {
    List<T> get();
}
