package org.tarantool.orm.sequence;

import org.tarantool.TarantoolClient;
import org.tarantool.orm.common.operation.result.TarantoolPrimitiveResultSetSync;
import org.tarantool.orm.common.operation.result.TarantoolResultSet;

import java.util.List;

/**
 * Created by GrIfOn on 03.01.2018.
 */
final public class TarantoolSequenceSync extends TarantoolSequence {
    public TarantoolSequenceSync(TarantoolClient client, String name) {
        super(client, name);
    }

    public TarantoolSequenceSync(TarantoolClient client, String name, long start) {
        super(client, name, start);
    }

    public TarantoolSequenceSync(TarantoolClient client, String name, long start, long min) {
        super(client, name, start, min);
    }

    public TarantoolSequenceSync(TarantoolClient client, String name, long start, long min, long max) {
        super(client, name, start, min, max);
    }

    public TarantoolSequenceSync(TarantoolClient client, String name, long start, long min, long max, long step) {
        super(client, name, start, min, max, step);
    }

    public TarantoolSequenceSync(TarantoolClient client, String name, long start, long min, long max, long step, boolean cycle) {
        super(client, name, start, min, max, step, cycle);
    }

    @Override
    public List<?> eval(String query) {
        return this.client.syncOps().eval(query);
    }

    @Override
    public TarantoolResultSet<Long> next() {
        return new TarantoolPrimitiveResultSetSync<>(eval(this.nextQuery()));
    }

    @Override
    public void alter(long start, long min, long max, boolean cycle, long step) {
        eval(this.alterQuery(start, min, max, cycle, step));
    }

    @Override
    public void reset() {
        eval(this.resetQuery());
    }

    @Override
    public void set(long value) {
        eval(this.setQuery(value));
    }

    @Override
    public void drop() {
        eval(this.dropQuery());
    }
}
