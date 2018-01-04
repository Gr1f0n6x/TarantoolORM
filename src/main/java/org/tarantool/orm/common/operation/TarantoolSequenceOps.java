package org.tarantool.orm.common.operation;

import org.tarantool.orm.common.operation.result.TarantoolResultSet;

/**
 * Created by GrIfOn on 03.01.2018.
 */
public interface TarantoolSequenceOps extends TarantoolOps {
    TarantoolResultSet<Long> next();
    void alter(long start, long min, long max, boolean cycle, long step);
    void reset();
    void set(long value);
    void drop();
}
