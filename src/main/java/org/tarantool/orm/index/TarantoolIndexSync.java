package org.tarantool.orm.index;

import org.tarantool.TarantoolClient;
import org.tarantool.orm.common.annotations.IndexField;
import org.tarantool.orm.common.operation.result.TarantoolPrimitiveResultSetSync;
import org.tarantool.orm.common.operation.result.TarantoolResultSet;
import org.tarantool.orm.common.operation.result.TarantoolTupleResultSetSync;
import org.tarantool.orm.common.type.CollationType;
import org.tarantool.orm.common.type.IndexType;
import org.tarantool.orm.common.type.IteratorType;
import org.tarantool.orm.entity.TarantoolTuple;

import java.util.List;
import java.util.Map;

/**
 * Created by GrIfOn on 03.01.2018.
 */
final public class TarantoolIndexSync<T extends TarantoolTuple> extends TarantoolIndex<T> {
    public TarantoolIndexSync(TarantoolClient client, String spaceName, Class<T> type, String indexName, List<IndexField> indexFields, Map<Integer, String> fields, IndexType indexType, boolean ifNotExists, boolean unique, CollationType collationType) {
        super(client, spaceName, type, indexName, indexFields, fields, indexType, ifNotExists, unique, collationType);
    }

    @Override
    public List<?> eval(String query) {
        return this.client.syncOps().eval(query);
    }

    @Override
    public TarantoolResultSet<T> min() {
        return new TarantoolTupleResultSetSync<>(eval(this.minQuery()), type, fields);
    }

    @Override
    public TarantoolResultSet<T> max() {
        return new TarantoolTupleResultSetSync<>(eval(this.maxQuery()), type, fields);    }

    @Override
    public TarantoolResultSet<T> min(T key) {
        return new TarantoolTupleResultSetSync<>(eval(this.minQuery(key)), type, fields);    }

    @Override
    public TarantoolResultSet<T> max(T key) {
        return new TarantoolTupleResultSetSync<>(eval(this.maxQuery(key)), type, fields);    }

    @Override
    public TarantoolResultSet<T> random(int seed) {
        return new TarantoolTupleResultSetSync<>(eval(this.randomQuery(seed)), type, fields);    }

    @Override
    public TarantoolResultSet<T> update(T tuple) {
        return new TarantoolTupleResultSetSync<>(eval(this.getQuery(tuple)), type, fields);    }

    @Override
    public TarantoolResultSet<T> delete(T tuple) {
        return new TarantoolTupleResultSetSync<>(eval(this.getQuery(tuple)), type, fields);    }

    @Override
    public TarantoolResultSet<Long> count(T key) {
        return new TarantoolPrimitiveResultSetSync<>(eval(this.countQuery(key)));
    }

    @Override
    public TarantoolResultSet<Long> count(T key, IteratorType type) {
        return new TarantoolPrimitiveResultSetSync<>(eval(this.countQuery(key, type)));
    }

    @Override
    public TarantoolResultSet<Long> bsize() {
        return new TarantoolPrimitiveResultSetSync<>(eval(this.bsizeQuery()));
    }

    @Override
    protected TarantoolResultSet<Integer> retrieveIndexId() {
        String query = String.format("return box.space.%s.index.%s.id", this.spaceName, this.name);
        return new TarantoolPrimitiveResultSetSync<>(eval(query));
    }
}
