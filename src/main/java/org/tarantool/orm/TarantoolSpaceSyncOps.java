package org.tarantool.orm;

import org.tarantool.TarantoolClient;
import org.tarantool.orm.exception.TarantoolNoSuchIndexException;
import org.tarantool.orm.exception.TarantoolORMException;
import org.tarantool.orm.type.IteratorType;

import java.util.List;

/**
 * Created by GrIfOn on 27.12.2017.
 */
public class TarantoolSpaceSyncOps<T extends TarantoolTuple> extends TarantoolSpace<T> {
    public TarantoolSpaceSyncOps(TarantoolClient client, Class<T> type, String spaceName) throws TarantoolORMException {
        super(client, type, spaceName);
    }

    public TarantoolSpaceSyncOps(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists) throws TarantoolORMException {
        super(client, type, spaceName, ifNotExists);
    }

    public TarantoolSpaceSyncOps(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists, int fieldCount) throws TarantoolORMException {
        super(client, type, spaceName, ifNotExists, fieldCount);
    }

    public TarantoolSpaceSyncOps(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists, int fieldCount, boolean temporary) throws TarantoolORMException {
        super(client, type, spaceName, ifNotExists, fieldCount, temporary);
    }

    @Override
    public List<?> eval(String query) {
        return this.client.syncOps().eval(query);
    }

    @Override
    public List<?> insert(T tuple) {
        return this.client
                .syncOps()
                .insert(
                        this.spaceId,
                        tuple.getValues()
                );
    }

    @Override
    public List<?> update(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return this.client
                .syncOps()
                .update(
                        this.spaceId,
                        usePrimaryIndex ? tuple.getIndexValues(this.primary.getName()) : tuple.getIndexValues(this.secondary.getName()),
                        tuple.getValuesForUpdate()
                );
        }

    @Override
    public List<?> replace(T tuple) {
        return this.client
                .syncOps()
                .replace(
                        this.spaceId,
                        tuple.getValues()
                );
    }

    @Override
    public List<?> delete(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return this.client
                .syncOps()
                .delete(
                        this.spaceId,
                        usePrimaryIndex ? tuple.getIndexValues(this.primary.getName()) : tuple.getIndexValues(this.secondary.getName())
                );
    }

    @Override
    public List<?> upsert(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return this.client
                .syncOps()
                .upsert(
                        this.spaceId,
                        usePrimaryIndex ? tuple.getIndexValues(this.primary.getName()) : tuple.getIndexValues(this.secondary.getName()),
                        tuple.getValues(),
                        tuple.getValuesForUpdate()
                        );
    }

    @Override
    public List<?> select(T tuple, boolean usePrimaryIndex, int offset, int limit, IteratorType iteratorType) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return this.client
                .syncOps()
                .select(
                        this.spaceId,
                        usePrimaryIndex ? this.primaryIndexId : this.secondaryIndexId,
                        tuple.getIndexValues(usePrimaryIndex ? this.primary.getName() : this.secondary.getName()),
                        offset,
                        limit,
                        iteratorType.getType()
                );
    }
}
