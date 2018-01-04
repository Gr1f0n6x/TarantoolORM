package org.tarantool.orm.index;

import org.tarantool.TarantoolClient;
import org.tarantool.orm.common.annotations.IndexField;
import org.tarantool.orm.common.operation.TarantoolIndexOps;
import org.tarantool.orm.common.type.IndexType;
import org.tarantool.orm.common.type.IteratorType;
import org.tarantool.orm.entity.TarantoolTuple;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by GrIfOn on 21.12.2017.
 */
public abstract class TarantoolIndex<T extends TarantoolTuple> implements TarantoolIndexOps<T> {
    private String spaceName;
    private String name;
    private IndexType indexType;
    private boolean ifNotExists;
    private boolean unique;
    private List<IndexField> indexFields;

    protected TarantoolClient client;
    protected Class<T> type;

    public TarantoolIndex(TarantoolClient client, String spaceName, Class<T> type, String indexName, List<IndexField> indexFields, IndexType indexType, boolean ifNotExists, boolean unique) {
        this.client = client;
        this.spaceName = spaceName;
        this.name = indexName;
        this.indexFields = indexFields;
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
                String.join(", ", indexFields.stream().map(x -> String.format("%d, '%s'", x.part(), x.type())).collect(Collectors.toList())));

        this.eval(query);
    }

    final public String getName() {
        return name;
    }

    final public String parts() {
        return this.indexFields
                .stream()
                .map(x -> String.format("IndexField{%d, %s}", x.part(), x.type().getType()))
                .collect(Collectors.toList())
                .toString();
    }

    final protected String selectQuery(T key, long offset, long limit, IteratorType iteratorType) {
        return String.format("box.space.%s.index.%s:select({%s}, {iterator=%s, offset=%d, limit=%d}})",
                this.spaceName,
                this.name,
                key.getIndexValues(this.name),
                iteratorType.getType(),
                offset,
                limit);
    }

    final protected String getQuery(T key) {
        return String.format("box.space.%s.index.%s:get({%s})",
                this.spaceName,
                this.name,
                key.getIndexValues(this.name));
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
        return String.format("return box.space.%s.index.%s:min({%s})", this.spaceName, this.name, key.getIndexValues(this.name));
    }

    final protected String maxQuery(T key) {
        return String.format("return box.space.%s.index.%s:max({%s})", this.spaceName, this.name, key.getIndexValues(this.name));
    }

    final protected String randomQuery(int seed) {
        return String.format("return box.space.%s.index.%s:random(%d)", this.spaceName, this.name, seed);
    }

    final protected String deleteQuery(T key) {
        return String.format("return box.space.%s.index.%s:delete({%s})", this.spaceName, this.name, key.getIndexValues(this.name));
    }

    final protected String updateQuery(T tuple) {
        return String.format("return box.space.%s.index.%s:update({%s})", this.spaceName, this.name, tuple.getValues());
    }

    final protected String countQuery(T key) {
        return String.format("return box.space.%s.index.%s:count({%s})",
                this.spaceName,
                this.name,
                key.getIndexValues(this.name));
    }

    final protected String countQuery(T key, IteratorType type) {
        return String.format("return box.space.%s.index.%s:count({%s}, {iterator = '%s'})",
                this.spaceName,
                this.name,
                key.getIndexValues(this.name),
                type.getName());
    }

    final protected String bsizeQuery() {
        return String.format("return box.space.%s.index.%s:bsize()", this.spaceName, this.name);
    }

    final protected String alterQuery(boolean unique, IndexType type) {
        return String.format("return box.space.%s.index.%s:alterIndex({type = '%s', unique = %s})", this.spaceName, this.name, type.getType(), unique);
    }

    final protected String renameQuery(String newName) {
        return String.format("return box.space.%s.index.%s:rename('%s')", this.spaceName, this.name, newName);
    }
}
