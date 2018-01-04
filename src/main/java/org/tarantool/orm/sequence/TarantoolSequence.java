package org.tarantool.orm.sequence;

import org.tarantool.TarantoolClient;
import org.tarantool.orm.common.operation.TarantoolSequenceOps;

/**
 * Created by GrIfOn on 03.01.2018.
 */
public abstract class TarantoolSequence implements TarantoolSequenceOps {
    private String name;
    private long start;
    private long min;
    private long max;
    private long step;
    private boolean cycle;

    protected TarantoolClient client;

    public TarantoolSequence(TarantoolClient client, String name) {
        this(client, name, 1);
    }

    public TarantoolSequence(TarantoolClient client, String name, long start) {
        this(client, name, start, 1);
    }

    public TarantoolSequence(TarantoolClient client, String name, long start, long min) {
        this(client, name, start, min, 9223372036854775807L);
    }

    public TarantoolSequence(TarantoolClient client, String name, long start, long min, long max) {
        this(client, name, start, min, max, 1);
    }

    public TarantoolSequence(TarantoolClient client, String name, long start, long min, long max, long step) {
        this(client, name, start, min, max, step, false);
    }

    public TarantoolSequence(TarantoolClient client, String name, long start, long min, long max, long step, boolean cycle) {
        this.client = client;
        this.name = name;
        this.start = start;
        this.min = min;
        this.max = max;
        this.step = step;
        this.cycle = cycle;

        String query = String.format("box.schema.sequence.create('%s', {start=%d, min=%d, max=%d, cycle=%s, step=%d})",
                name,
                start,
                min,
                max,
                cycle,
                step);

        this.eval(query);
    }

    public String nextQuery() {
        return String.format("box.sequence.%s:next()", this.name);
    }

    public String alterQuery(long start, long min, long max, boolean cycle, long step) {
        return String.format("box.sequence.%s:alter({start=%d, min=%d, max=%d, cycle=%s, step=%d})",
                this.name,
                start,
                min,
                max,
                cycle,
                step);
    }

    public String resetQuery() {
        return String.format("box.sequence.%s:reset()", this.name);
    }

    public String setQuery(long value) {
        return String.format("box.sequence.%s:set(%d)", this.name, value);
    }

    public String dropQuery() {
        return String.format("box.sequence.%s:drop()", this.name);
    }
}
