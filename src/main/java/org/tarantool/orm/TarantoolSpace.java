package org.tarantool.orm;

import org.tarantool.orm.annotation.IndexField;
import org.tarantool.orm.exception.TarantoolNoSuchIndexException;
import org.tarantool.orm.exception.TarantoolORMException;
import org.tarantool.TarantoolClient;
import org.tarantool.orm.type.IndexType;
import org.tarantool.orm.type.IteratorType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by GrIfOn on 20.12.2017.
 */
public abstract class TarantoolSpace<T extends TarantoolTuple> {
    private boolean ifNotExists;
    private boolean temporary;
    protected Class<T> type;
    private Map<String, List<IndexField>> indexFields;

    protected TarantoolClient client;
    protected String spaceName;
    protected int fieldCount;
    protected int spaceId;

    protected TarantoolIndex primary;
    protected TarantoolIndex secondary;

    protected int primaryIndexId;
    protected int secondaryIndexId;

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

    final public Map<String, List<IndexField>> getIndexFields() {
        return indexFields;
    }

    final public void createIndex(TarantoolIndex index, boolean primary) {
        String query = index.createIndex(this.spaceName, this.indexFields.get(index.getName()));
        this.eval(query);
        int indexId = getId(String.format("return box.space.%s.index.%s.id", this.spaceName, index.getName()));
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

    final public int getPrimaryIndexId() {
        return primaryIndexId;
    }

    final public int getSecondaryIndexId() {
        return secondaryIndexId;
    }

    final public Object dropIndex(boolean primary) {
        if (primary) {
            String query = this.primary.dropIndex(this.spaceName);
            return eval(query);
        } else {
            String query = this.secondary.dropIndex(this.spaceName);
            return eval(query);
        }
    }

    final public Object min(boolean primary) {
        if (primary) {
            String query = this.primary.min(this.spaceName);
            return eval(query);
        } else {
            String query = this.secondary.min(this.spaceName);
            return eval(query);
        }
    }

    final public Object max(boolean primary) {
        if (primary) {
            String query = this.primary.max(this.spaceName);
            return eval(query);
        } else {
            String query = this.secondary.max(this.spaceName);
            return eval(query);
        }
    }

    final public Object random(boolean primary, int seed) {
        if (primary) {
            String query = this.primary.random(this.spaceName, seed);
            return eval(query);
        } else {
            String query = this.secondary.random(this.spaceName, seed);
            return eval(query);
        }
    }

    final public Object count(boolean primary, List<?> key) {
        if (primary) {
            String query = this.primary.count(this.spaceName, key);
            return eval(query);
        } else {
            String query = this.secondary.count(this.spaceName, key);
            return eval(query);
        }
    }

    final public Object count(boolean primary, List<?> key, IteratorType type) {
        if (primary) {
            String query = this.primary.count(this.spaceName, key, type);
            return eval(query);
        } else {
            String query = this.secondary.count(this.spaceName, key, type);
            return eval(query);
        }
    }

    final public Object indexBsize(boolean primary) {
        if (primary) {
            String query = this.primary.bsize(this.spaceName);
            return eval(query);
        } else {
            String query = this.secondary.bsize(this.spaceName);
            return eval(query);
        }
    }

    final public Object alter(boolean primary, boolean unique, IndexType type) {
        if (primary) {
            String query = this.primary.alter(this.spaceName, unique, type);
            return eval(query);
        } else {
            String query = this.secondary.alter(this.spaceName, unique, type);
            return eval(query);
        }
    }

    public abstract Object eval(String query);

    public abstract TarantoolQueryResult<T> insert(T tuple);

    public abstract TarantoolQueryResult<T> update(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException;

    public abstract TarantoolQueryResult<T> replace(T tuple);

    public abstract TarantoolQueryResult<T> delete(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException;

    public abstract TarantoolQueryResult<T> upsert(T tuple, boolean usePrimaryIndex) throws TarantoolNoSuchIndexException;

    public abstract TarantoolQueryResult<T> select(T tuple, boolean usePrimaryIndex, int offset, int limit, IteratorType iteratorType) throws TarantoolNoSuchIndexException;

    private Map<String, List<IndexField>> indexFields() {
        return Stream.of(this.type.getDeclaredFields())
                .filter(x -> {
                    x.setAccessible(true);
                    return x.getType().equals(TarantoolField.class) && x.isAnnotationPresent(IndexField.class);
                })
                .map(x -> x.getAnnotation(IndexField.class))
                .collect(Collectors.groupingBy(IndexField::indexName));
    }

    private void initSpace() {
        eval(String.format("box.schema.space.create('%s', {temporary=%s, if_not_exists=%s, field_count=%d})",
                this.spaceName,
                this.temporary,
                this.ifNotExists,
                this.fieldCount));

        this.spaceId = getId(String.format("return box.space.%s.id", this.spaceName));
    }

    protected abstract Integer getId(String query);
}
