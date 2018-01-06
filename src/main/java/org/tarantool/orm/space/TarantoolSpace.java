package org.tarantool.orm.space;

import org.tarantool.orm.common.annotations.Index;
import org.tarantool.orm.common.annotations.Indexes;
import org.tarantool.orm.entity.TarantoolField;
import org.tarantool.orm.index.TarantoolIndex;
import org.tarantool.orm.common.operation.result.TarantoolResultSet;
import org.tarantool.orm.entity.TarantoolTuple;
import org.tarantool.orm.common.annotations.IndexField;
import org.tarantool.orm.common.exception.TarantoolIndexNullPointerException;
import org.tarantool.orm.common.exception.TarantoolORMException;
import org.tarantool.TarantoolClient;
import org.tarantool.orm.common.type.IteratorType;

import java.util.List;
import java.util.Map;
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
    protected Map<Integer, String> fields;

    protected TarantoolClient client;
    protected String spaceName;
    protected int fieldCount;
    protected int spaceId;

    protected TarantoolIndex<T> primary;
    protected TarantoolIndex<T> secondary;

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
        this.fields = TarantoolTuple.retrieveFieldMap(type);
        if (this.fields.size() != fieldCount) {
            throw new TarantoolORMException("Field count should equals to fields size");
        }

        this.indexFields = this.indexFields();

        initSpace();
        initIndexes();
    }

    final public TarantoolIndex<T> index(boolean primary) {
        return primary ? this.primary : this.secondary;
    }

    final public Map<String, List<IndexField>> getIndexFields() {
        return indexFields;
    }

    private void initIndexes() {
        Indexes indexes = type.getAnnotation(Indexes.class);

        for(Index index : indexes.indexList()) {
            if(this.primary == null) {
                primary = createIndex(index);
            } else if(secondary == null) {
                secondary = createIndex(index);
            } else {
                break;
            }
        }
    }

    protected abstract TarantoolIndex<T> createIndex(Index index);

    public abstract Object eval(String query);

    public abstract TarantoolResultSet<T> insert(T tuple);

    public abstract TarantoolResultSet<T> update(T tuple, boolean usePrimaryIndex) throws TarantoolIndexNullPointerException;

    public abstract TarantoolResultSet<T> replace(T tuple);

    public abstract TarantoolResultSet<T> delete(T tuple, boolean usePrimaryIndex) throws TarantoolIndexNullPointerException;

    public abstract TarantoolResultSet<T> upsert(T tuple, boolean usePrimaryIndex) throws TarantoolIndexNullPointerException;

    public abstract TarantoolResultSet<T> select(T tuple, boolean usePrimaryIndex, int offset, int limit, IteratorType iteratorType) throws TarantoolIndexNullPointerException;

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
