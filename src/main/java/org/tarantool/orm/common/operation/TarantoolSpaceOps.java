package org.tarantool.orm.common.operation;

import org.tarantool.orm.entity.TarantoolTuple;
import org.tarantool.orm.common.operation.result.TarantoolResultSet;

/**
 * Created by GrIfOn on 03.01.2018.
 */
public interface TarantoolSpaceOps<T extends TarantoolTuple> extends TarantoolOps<T> {
    TarantoolResultSet<Long> bsize();
    TarantoolResultSet<Long> count();
    TarantoolResultSet<T> createIndex();
    TarantoolResultSet<T> delete();
    TarantoolResultSet<T> drop();
    TarantoolResultSet<T> format();
    TarantoolResultSet<T> get();
    TarantoolResultSet<T> insert();
    TarantoolResultSet<Long> len();
    TarantoolResultSet<T> on_replace();
    TarantoolResultSet<T> pairs();
    TarantoolResultSet<T> put();
    TarantoolResultSet<T> rename();
    TarantoolResultSet<T> replace();
    TarantoolResultSet<T> run_triggers();
    TarantoolResultSet<T> select();
    TarantoolResultSet<T> truncate();
    TarantoolResultSet<T> update();
    TarantoolResultSet<T> upsert();
    TarantoolResultSet<T> enabled();
    TarantoolResultSet<Long> field_count();
    TarantoolResultSet<Long> id();
    TarantoolResultSet<T> index();
}