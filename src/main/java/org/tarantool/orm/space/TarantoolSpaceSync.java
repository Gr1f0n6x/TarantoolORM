package org.tarantool.orm.space;

import org.tarantool.TarantoolClient;
import org.tarantool.orm.common.annotations.Index;
import org.tarantool.orm.common.operation.result.TarantoolResultSet;
import org.tarantool.orm.common.operation.result.TarantoolTupleResultSetSync;
import org.tarantool.orm.entity.TarantoolTuple;
import org.tarantool.orm.common.exception.TarantoolIndexNullPointerException;
import org.tarantool.orm.common.exception.TarantoolORMException;
import org.tarantool.orm.common.type.IteratorType;
import org.tarantool.orm.index.TarantoolIndex;
import org.tarantool.orm.index.TarantoolIndexSync;

import java.util.Arrays;
import java.util.List;

/**
 * Created by GrIfOn on 27.12.2017.
 */
final public class TarantoolSpaceSync<T extends TarantoolTuple> extends TarantoolSpace<T> {
    public TarantoolSpaceSync(TarantoolClient client, Class<T> type, String spaceName) throws TarantoolORMException {
        super(client, type, spaceName);
    }

    public TarantoolSpaceSync(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists) throws TarantoolORMException {
        super(client, type, spaceName, ifNotExists);
    }

    public TarantoolSpaceSync(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists, int fieldCount) throws TarantoolORMException {
        super(client, type, spaceName, ifNotExists, fieldCount);
    }

    public TarantoolSpaceSync(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists, int fieldCount, boolean temporary) throws TarantoolORMException {
        super(client, type, spaceName, ifNotExists, fieldCount, temporary);
    }

    @Override
    public List<?> eval(String query) {
        return this.client.syncOps().eval(query);
    }

    @Override
    public TarantoolResultSet<T> insert(T tuple) {
        return new TarantoolTupleResultSetSync<>(this.client
                .syncOps()
                .insert(
                        this.spaceId,
                        tuple.getValues(fields)
                ), type, fields);
    }

    @Override
    public TarantoolResultSet<T> update(T tuple, boolean usePrimaryIndex) throws TarantoolIndexNullPointerException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolIndexNullPointerException();

        return new TarantoolTupleResultSetSync<>(this.client
                .syncOps()
                .update(
                        this.spaceId,
                        tuple.getIndexValues(fields, usePrimaryIndex? this.primary.getName() : this.secondary.getName()),
                        tuple.getValuesForUpdate(fields)
                ), type, fields);
        }

    @Override
    public TarantoolResultSet<T> replace(T tuple) {
        return new TarantoolTupleResultSetSync<>(this.client
                .syncOps()
                .replace(
                        this.spaceId,
                        tuple.getValues(fields)
                ), type, fields);
    }

    @Override
    public TarantoolResultSet<T> delete(T tuple, boolean usePrimaryIndex) throws TarantoolIndexNullPointerException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolIndexNullPointerException();

        return new TarantoolTupleResultSetSync<>(this.client
                .syncOps()
                .delete(
                        this.spaceId,
                        tuple.getIndexValues(fields, usePrimaryIndex ? this.primary.getName() : this.secondary.getName())
                ), type, fields);
    }

    @Override
    public TarantoolResultSet<T> upsert(T tuple, boolean usePrimaryIndex) throws TarantoolIndexNullPointerException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolIndexNullPointerException();

        return new TarantoolTupleResultSetSync<>(this.client
                .syncOps()
                .upsert(
                        this.spaceId,
                        tuple.getIndexValues(fields, usePrimaryIndex ? this.primary.getName() : this.secondary.getName()),
                        tuple.getValues(fields),
                        tuple.getValuesForUpdate(fields)
                ), type, fields);
    }

    @Override
    public TarantoolResultSet<T> select(T tuple, boolean usePrimaryIndex, int offset, int limit, IteratorType iteratorType) throws TarantoolIndexNullPointerException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolIndexNullPointerException();

        return new TarantoolTupleResultSetSync<>(this.client
                .syncOps()
                .select(
                        this.spaceId,
                        usePrimaryIndex ? this.primary.getIndexId() : this.secondary.getIndexId(),
                        tuple.getIndexValues(fields, usePrimaryIndex ? this.primary.getName() : this.secondary.getName()),
                        offset,
                        limit,
                        iteratorType.getType()
                ), type, fields);
    }

    @Override
    protected Integer getId(String query) {
        return (Integer) eval(query).get(0);
    }

    @Override
    protected TarantoolIndex<T> createIndex(Index index) {
        return new TarantoolIndexSync<>(this.client,
                this.spaceName,
                this.type,
                index.name(),
                getIndexFields().get(index.name()),
                fields,
                index.type(),
                index.ifNotExists(),
                index.unique(),
                index.collationType()
        );
    }
}
