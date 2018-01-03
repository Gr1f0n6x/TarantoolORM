package org.tarantool.orm.space;

import org.tarantool.TarantoolClient;
import org.tarantool.orm.query.TarantoolResultSet;
import org.tarantool.orm.entity.TarantoolTuple;
import org.tarantool.orm.exception.TarantoolNoSuchIndexException;
import org.tarantool.orm.exception.TarantoolORMException;
import org.tarantool.orm.type.IteratorType;

import java.util.List;
import java.util.stream.Collectors;

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
        return new TarantoolTupleResultSet<>(this.client
                .syncOps()
                .insert(
                        this.spaceId,
                        tuple.getValues()
                ));
    }

    @Override
    public TarantoolResultSet<T> update(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return new TarantoolTupleResultSet<>(this.client
                .syncOps()
                .update(
                        this.spaceId,
                        usePrimaryIndex ? tuple.getIndexValues(this.primary.getName()) : tuple.getIndexValues(this.secondary.getName()),
                        tuple.getValuesForUpdate()
                ));
        }

    @Override
    public TarantoolResultSet<T> replace(T tuple) {
        return new TarantoolTupleResultSet<>(this.client
                .syncOps()
                .replace(
                        this.spaceId,
                        tuple.getValues()
                ));
    }

    @Override
    public TarantoolResultSet<T> delete(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return new TarantoolTupleResultSet<>(this.client
                .syncOps()
                .delete(
                        this.spaceId,
                        usePrimaryIndex ? tuple.getIndexValues(this.primary.getName()) : tuple.getIndexValues(this.secondary.getName())
                ));
    }

    @Override
    public TarantoolResultSet<T> upsert(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return new TarantoolTupleResultSet<>(this.client
                .syncOps()
                .upsert(
                        this.spaceId,
                        usePrimaryIndex ? tuple.getIndexValues(this.primary.getName()) : tuple.getIndexValues(this.secondary.getName()),
                        tuple.getValues(),
                        tuple.getValuesForUpdate()
                ));
    }

    @Override
    public TarantoolResultSet<T> select(T tuple, boolean usePrimaryIndex, int offset, int limit, IteratorType iteratorType) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return new TarantoolTupleResultSet<>(this.client
                .syncOps()
                .select(
                        this.spaceId,
                        usePrimaryIndex ? this.primaryIndexId : this.secondaryIndexId,
                        tuple.getIndexValues(usePrimaryIndex ? this.primary.getName() : this.secondary.getName()),
                        offset,
                        limit,
                        iteratorType.getType()
                ));
    }

    @Override
    public TarantoolResultSet<T> min(boolean primary) {
        String query;

        if (primary) query = this.primary.min(this.spaceName);
        else query = this.secondary.min(this.spaceName);

        return new TarantoolTupleResultSet<>(eval(query));
    }

    @Override
    public TarantoolResultSet<T> max(boolean primary) {
        String query;

        if (primary) query = this.primary.max(this.spaceName);
        else query = this.secondary.max(this.spaceName);

        return new TarantoolTupleResultSet<>(eval(query));
    }

    @Override
    public TarantoolResultSet<T> random(boolean primary, int seed) {
        String query;

        if (primary) query = this.primary.random(this.spaceName, seed);
        else query = this.secondary.random(this.spaceName, seed);

        return new TarantoolTupleResultSet<>(eval(query));
    }

    @Override
    public TarantoolResultSet<Integer> count(boolean primary, T key) {
        if (primary) {
            String query = this.primary.count(this.spaceName, key.getIndexValues(primary ? this.primary.getName() : this.secondary.getName()));
            return new TarantoolScalarResultSet<>(eval(query));
        } else {
            String query = this.secondary.count(this.spaceName, key.getIndexValues(primary ? this.primary.getName() : this.secondary.getName()));
            return new TarantoolScalarResultSet<>(eval(query));
        }
    }

    @Override
    public TarantoolResultSet<Integer> count(boolean primary, T key, IteratorType type) {
        if (primary) {
            String query = this.primary.count(this.spaceName, key.getIndexValues(primary ? this.primary.getName() : this.secondary.getName()), type);
            return new TarantoolScalarResultSet<>(eval(query));
        } else {
            String query = this.secondary.count(this.spaceName, key.getIndexValues(primary ? this.primary.getName() : this.secondary.getName()), type);
            return new TarantoolScalarResultSet<>(eval(query));
        }
    }

    @Override
    public TarantoolResultSet<Integer> indexBsize(boolean primary) {
        if (primary) {
            String query = this.primary.bsize(this.spaceName);
            return new TarantoolScalarResultSet<>(eval(query));
        } else {
            String query = this.secondary.bsize(this.spaceName);
            return new TarantoolScalarResultSet<>(eval(query));
        }
    }

    @Override
    protected Integer getId(String query) {
        return (Integer) eval(query).get(0);
    }

    private class TarantoolTupleResultSet<R extends TarantoolTuple> implements TarantoolResultSet<R> {
        private List<?> resultSet;

        private TarantoolTupleResultSet(List<?> resultSet) {
            this.resultSet = resultSet;
        }

        @Override
        public List<R> get() {
            return resultSet.stream()
                    .map(values -> {
                        try {
                            return (R) TarantoolTuple.build(type, (List<?>) values);
                        } catch (IllegalAccessException | InstantiationException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    private class TarantoolScalarResultSet<R> implements TarantoolResultSet<R> {
        private List<?> resultSet;

        private TarantoolScalarResultSet(List<?> resultSet) {
            this.resultSet = resultSet;
        }

        @Override
        public List<R> get() {
            return resultSet.stream()
                    .map(value -> (R) value)
                    .collect(Collectors.toList());
        }
    }
}
