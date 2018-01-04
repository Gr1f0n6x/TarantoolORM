package org.tarantool.orm.common.operation;

import org.tarantool.orm.common.type.IndexType;
import org.tarantool.orm.common.type.IteratorType;
import org.tarantool.orm.entity.TarantoolTuple;
import org.tarantool.orm.common.operation.result.TarantoolResultSet;

/**
 * Created by GrIfOn on 03.01.2018.
 */
public interface TarantoolIndexOps<T extends TarantoolTuple> extends TarantoolOps<T> {
    TarantoolResultSet<T> select(T key, long offset, long limit, IteratorType iteratorType);
    TarantoolResultSet<T> get(T key);
    TarantoolResultSet<T> min();
    TarantoolResultSet<T> min(T key);
    TarantoolResultSet<T> max();
    TarantoolResultSet<T> max(T key);
    TarantoolResultSet<T> random(int seed);
    TarantoolResultSet<T> update(T tuple);
    TarantoolResultSet<T> delete(T tuple);
    TarantoolResultSet<Long> count(T key);
    TarantoolResultSet<Long> count(T key, IteratorType type);
    TarantoolResultSet<Long> bsize();
    void alter(boolean unique, IndexType type);
    void drop();
    void rename(String newName);
}
