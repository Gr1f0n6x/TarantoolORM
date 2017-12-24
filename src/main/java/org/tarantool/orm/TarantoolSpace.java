package org.tarantool.orm;

import org.tarantool.orm.annotation.IndexField;
import org.tarantool.orm.exception.TarantoolNoSuchIndexException;
import org.tarantool.orm.exception.TarantoolORMException;
import org.tarantool.TarantoolClient;
import org.tarantool.orm.type.IteratorType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by GrIfOn on 20.12.2017.
 */
final public class TarantoolSpace<T extends TarantoolTuple> {
    private TarantoolClient client;
    private String spaceName;
    private boolean ifNotExists;
    private boolean temporary;
    private int fieldCount;
    private int spaceId;

    private Class<T> type;

    private Map<String, List<IndexField>> indexFields;

    private TarantoolIndex primary;
    private TarantoolIndex secondary;

    private int primaryIndexId;
    private int secondaryIndexId;

    public TarantoolSpace(TarantoolClient client, Class<T> type, String spaceName) throws TarantoolORMException {
        this(client, type, spaceName, false, 0, false);
    }

    public TarantoolSpace(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists) throws TarantoolORMException {
        this(client, type, spaceName, ifNotExists, 0, false);
    }

    public TarantoolSpace(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists, int fieldCount) throws TarantoolORMException {
        this(client, type, spaceName, ifNotExists, fieldCount, false);
    }

    public TarantoolSpace(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists, int fieldCount, boolean temporary) throws TarantoolORMException {
        if (fieldCount < 0) {
            throw new TarantoolORMException("Field count should be >= 0");
        }

        this.spaceName = spaceName;
        this.client = client;
        this.ifNotExists = ifNotExists;
        this.fieldCount = fieldCount;
        this.temporary = temporary;

        this.type = type;

        this.indexFields = this.indexFields();

        this.initSpace();
    }

    public Map<String, List<IndexField>> getIndexFields() {
        return indexFields;
    }

    public void createIndex(TarantoolIndex index, boolean primary) {
        String query = index.createIndex(this.spaceName, this.indexFields.get(index.getName()));
        this.client.syncOps().eval(query);
        int indexId = (Integer) eval(String.format("return box.space.%s.index.%s.id", this.spaceName, index.getName())).get(0);
        if (primary) {
            if (this.primary != null) {
                String dropQuery = this.primary.dropIndex(this.spaceName);
                eval(dropQuery);
            }
            this.primary = index;
            this.primaryIndexId = indexId;
        } else {
            if (this.secondary != null) {
                String dropQuery = this.secondary.dropIndex(this.spaceName);
                eval(dropQuery);
            }
            this.secondary = index;
            this.secondaryIndexId = indexId;
        }
    }

    public List<?> eval(String query) {
        return this.client.syncOps().eval(query);
    }

    public Future<List<?>> asyncEval(String query) {
        return this.client.asyncOps().eval(query);
    }

    public int getPrimaryIndexId() {
        return primaryIndexId;
    }

    public int getSecondaryIndexId() {
        return secondaryIndexId;
    }

    public List<?> insert(T tuple) {
        return this.client
                .syncOps()
                .insert(
                        this.spaceId,
                        tuple.getValues()
                );
    }

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

    public List<?> replace(T tuple) {
        return this.client
                .syncOps()
                .replace(
                        this.spaceId,
                        tuple.getValues()
                );

    }

    public List<?> delete(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return this.client
                .syncOps()
                .delete(
                        this.spaceId,
                        usePrimaryIndex ? tuple.getIndexValues(this.primary.getName()) : tuple.getIndexValues(this.secondary.getName())
                );
    }

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

    public Future<List<?>> asyncInsert(T tuple) {
        return this.client
                .asyncOps()
                .insert(
                        this.spaceId,
                        tuple.getValues()
                );
    }

    public Future<List<?>> asyncUpdate(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return this.client
                .asyncOps()
                .update(
                        this.spaceId,
                        usePrimaryIndex ? tuple.getIndexValues(this.primary.getName()) : tuple.getIndexValues(this.secondary.getName()),
                        tuple.getValuesForUpdate()
                );
    }

    public Future<List<?>> asyncReplace(T tuple) {
        return this.client
                .asyncOps()
                .replace(
                        this.spaceId,
                        tuple.getValues()
                );
    }

    public Future<List<?>> asyncDelete(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
        if (usePrimaryIndex && this.primary == null || !usePrimaryIndex && this.secondary == null) throw new TarantoolNoSuchIndexException();

        return this.client.asyncOps().delete(
                this.spaceId,
                usePrimaryIndex ? tuple.getIndexValues(this.primary.getName()) : tuple.getIndexValues(this.secondary.getName())
        );
    }

    public Future<List<?>> asyncUpsert(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException {
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

    public Future<List<?>> asyncSelect(T tuple, boolean usePrimaryIndex, int offset, int limit, IteratorType iteratorType) throws TarantoolNoSuchIndexException {
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

    private void initSpace() {
        eval(String.format("box.schema.space.create('%s', {temporary=%s, if_not_exists=%s, field_count=%d})",
                this.spaceName,
                this.temporary,
                this.ifNotExists,
                this.fieldCount));

        this.spaceId = (Integer) eval(String.format("return box.space.%s.id", this.spaceName)).get(0);
    }

    private Map<String, List<IndexField>> indexFields() {
        return Stream.of(this.type.getDeclaredFields())
                .filter(x -> {
                    x.setAccessible(true);
                    return x.getType().equals(TarantoolField.class) && x.isAnnotationPresent(IndexField.class);
                })
                .map(x -> x.getAnnotation(IndexField.class))
                .collect(Collectors.groupingBy(IndexField::indexName));
    }
}
