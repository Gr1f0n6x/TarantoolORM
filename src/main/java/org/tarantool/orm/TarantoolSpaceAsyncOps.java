package org.tarantool.orm;

import org.tarantool.TarantoolClient;
import org.tarantool.orm.exception.TarantoolNoSuchIndexException;
import org.tarantool.orm.exception.TarantoolORMException;
import org.tarantool.orm.type.IteratorType;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by GrIfOn on 27.12.2017.
 */
public class TarantoolSpaceAsyncOps<T extends TarantoolTuple> extends TarantoolSpace<T> {
    public TarantoolSpaceAsyncOps(TarantoolClient client, Class<T> type, String spaceName) throws TarantoolORMException {
        super(client, type, spaceName);
    }

    public TarantoolSpaceAsyncOps(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists) throws TarantoolORMException {
        super(client, type, spaceName, ifNotExists);
    }

    public TarantoolSpaceAsyncOps(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists, int fieldCount) throws TarantoolORMException {
        super(client, type, spaceName, ifNotExists, fieldCount);
    }

    public TarantoolSpaceAsyncOps(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists, int fieldCount, boolean temporary) throws TarantoolORMException {
        super(client, type, spaceName, ifNotExists, fieldCount, temporary);
    }

    @Override
    public Future<List<?>> eval(String query) {
        return this.client.asyncOps().eval(query);
    }

    @Override
    public Future<List<?>> insert(T tuple) {
        return this.client
                .asyncOps()
                .insert(
                        this.spaceId,
                        tuple.getValues()
                );
    }

    @Override
    public Future<List<?>> update(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return this.client
                .asyncOps()
                .update(
                        this.spaceId,
                        usePrimaryIndex ? tuple.getIndexValues(this.primary.getName()) : tuple.getIndexValues(this.secondary.getName()),
                        tuple.getValuesForUpdate()
                );
    }

    @Override
    public Future<List<?>> replace(T tuple) {
        return this.client
                .asyncOps()
                .replace(
                        this.spaceId,
                        tuple.getValues()
                );
    }

    @Override
    public Future<List<?>> delete(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return this.client.asyncOps().delete(
                this.spaceId,
                usePrimaryIndex ? tuple.getIndexValues(this.primary.getName()) : tuple.getIndexValues(this.secondary.getName())
        );
    }

    @Override
    public Future<List<?>> upsert(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return this.client
                .asyncOps()
                .upsert(
                        this.spaceId,
                        usePrimaryIndex ? tuple.getIndexValues(this.primary.getName()) : tuple.getIndexValues(this.secondary.getName()),
                        tuple.getValues(),
                        tuple.getValuesForUpdate()
                );
    }

    @Override
    public Future<List<?>> select(T tuple, boolean usePrimaryIndex, int offset, int limit, IteratorType iteratorType) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return this.client.asyncOps().select(
                this.spaceId,
                usePrimaryIndex ? this.primaryIndexId : this.secondaryIndexId,
                tuple.getIndexValues(usePrimaryIndex ? this.primary.getName() : this.secondary.getName()),
                offset,
                limit,
                iteratorType.getType()
        );
    }
}
