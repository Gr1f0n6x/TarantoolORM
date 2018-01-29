package org.tarantool.orm.index;

import org.tarantool.TarantoolClient;
import org.tarantool.orm.common.annotations.IndexField;
import org.tarantool.orm.common.operation.TarantoolIndexOps;
import org.tarantool.orm.common.operation.result.TarantoolResultSet;
import org.tarantool.orm.common.type.CollationType;
import org.tarantool.orm.common.type.IndexType;
import org.tarantool.orm.common.type.IteratorType;
import org.tarantool.orm.common.type.TarantoolType;
import org.tarantool.orm.entity.TarantoolTuple;
import org.tarantool.orm.space.TarantoolSpace;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by GrIfOn on 21.12.2017.
 */
public abstract class TarantoolIndex<T extends TarantoolTuple> implements TarantoolIndexOps<T> {
    protected String name;
    protected int indexId;
    protected IndexType indexType;
    protected boolean ifNotExists;
    protected boolean unique;
    protected List<TarantoolSpace.Tuple> indexFields;
    protected Map<Integer, String> fields;

    protected TarantoolClient client;
    protected Class<T> type;
    protected String spaceName;

    public TarantoolIndex(TarantoolClient client, String spaceName, Class<T> type, String indexName, List<TarantoolSpace.Tuple> indexFields, Map<Integer, String> fields, IndexType indexType, boolean ifNotExists, boolean unique) {
        this.client = client;
        this.spaceName = spaceName;
        this.name = indexName;
        this.indexFields = indexFields;
        this.fields = fields;
        this.indexType = indexType;
        this.type = type;
        this.ifNotExists = ifNotExists;
        this.unique = unique;

        String query = String.format("box.space.%s:create_index('%s', {type='%s', if_not_exists=%s, unique=%s, parts={%s}})",
                this.spaceName,
                this.name,
                this.indexType.getType(),
                this.ifNotExists,
                this.unique,
                String.join(", ", indexFields.stream().map(this::formatPart).collect(Collectors.toList()))
                );

        this.eval(query);

        this.indexId = retrieveIndexId().get().get(0);
    }

    final public String getName() {
        return name;
    }

    final public IndexType getIndexType() {
        return indexType;
    }

    final public boolean isIfNotExists() {
        return ifNotExists;
    }

    final public boolean isUnique() {
        return unique;
    }

    final public int getIndexId() {
        return indexId;
    }

    final public String parts() {
        return this.indexFields
                .stream()
                .map(x -> String.format("IndexField{%d, %s}", x.part, x.type.getType()))
                .collect(Collectors.toList())
                .toString();
    }

    final protected String getQuery(T key) {
        return String.format("return box.space.%s.index.%s:get({%s})",
                this.spaceName,
                this.name,
                values2String(key.getIndexValues(fields, this.name)));
    }

    final protected String dropQuery() {
        return String.format("box.space.%s.index.%s:drop()", this.spaceName, this.name);
    }

    final protected String minQuery() {
        return String.format("return box.space.%s.index.%s:min()", this.spaceName, this.name);
    }

    final protected String maxQuery() {
        return String.format("return box.space.%s.index.%s:max()", this.spaceName, this.name);
    }

    final protected String minQuery(T key) {
        return String.format("return box.space.%s.index.%s:min({%s})", this.spaceName, this.name, values2String(key.getIndexValues(fields, this.name)));
    }

    final protected String maxQuery(T key) {
        return String.format("return box.space.%s.index.%s:max({%s})", this.spaceName, this.name, values2String(key.getIndexValues(fields, this.name)));
    }

    final protected String randomQuery(int seed) {
        return String.format("return box.space.%s.index.%s:random(%d)", this.spaceName, this.name, seed);
    }

    final protected String deleteQuery(T key) {
        return String.format("return box.space.%s.index.%s:delete({%s})", this.spaceName, this.name, values2String(key.getIndexValues(fields, this.name)));
    }

    final protected String updateQuery(T tuple) {
        return String.format("return box.space.%s.index.%s:update({%s})", this.spaceName, this.name, tuple.getValues(fields));
    }

    final protected String countQuery(T key) {
        return String.format("return box.space.%s.index.%s:count({%s})",
                this.spaceName,
                this.name,
                values2String(key.getIndexValues(fields, this.name)));
    }

    final protected String countQuery(T key, IteratorType type) {
        return String.format("return box.space.%s.index.%s:count({%s}, {iterator = '%s'})",
                this.spaceName,
                this.name,
                values2String(key.getIndexValues(fields, this.name)),
                type.getName());
    }

    final protected String bsizeQuery() {
        return String.format("return box.space.%s.index.%s:bsize()", this.spaceName, this.name);
    }

    final protected String alterQuery(boolean unique, IndexType type) {
        return String.format("return box.space.%s.index.%s:alter({type = '%s', unique = %s})", this.spaceName, this.name, type.getType(), unique);
    }

    final protected String renameQuery(String newName) {
        return String.format("return box.space.%s.index.%s:rename('%s')", this.spaceName, this.name, newName);
    }

    final public void alter(boolean unique, IndexType type) {
        eval(this.alterQuery(unique, type));
    }

    final public void drop() {
        eval(this.dropQuery());
    }

    final public void rename(String newName) {
        eval(this.renameQuery(newName));
    }

    abstract protected TarantoolResultSet<Integer> retrieveIndexId();

    private String formatPart(TarantoolSpace.Tuple tuple) {
        if(tuple.type == TarantoolType.STRING && tuple.collationType != CollationType.BINARY) {
            return String.format("{%d, '%s', is_nullable=%s, collation='%s'}", tuple.part, tuple.type, tuple.field.isNullable(), tuple.collationType.getName());
        }

        return String.format("{%d, '%s', is_nullable=%s}", tuple.part, tuple.type, tuple.field.isNullable());
    }

    private String values2String(List<?> values) {
        return String.join(", ", values.stream().map(x -> {
            if (x.getClass().isArray()) {
                return Arrays.toString((Object[]) x);
            } else if (x.getClass().equals(String.class)) {
                return String.format("'%s'", x.toString());
            }
            return x.toString();
        }).collect(Collectors.toList()));
    }
}
