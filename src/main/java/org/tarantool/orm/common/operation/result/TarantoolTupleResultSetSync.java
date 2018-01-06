package org.tarantool.orm.common.operation.result;

import org.tarantool.orm.entity.TarantoolTuple;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by GrIfOn on 04.01.2018.
 */
final public class TarantoolTupleResultSetSync<T extends TarantoolTuple> implements TarantoolResultSet<T> {
    private List<?> resultSet;
    private Class<T> type;
    private Map<Integer, String> fields;

    public TarantoolTupleResultSetSync(List<?> resultSet, Class<T> type, Map<Integer, String> fields) {
        this.resultSet = resultSet;
        this.type = type;
        this.fields = fields;
    }

    @Override
    public List<T> get() {
        return resultSet.stream()
                .map(values -> (T) TarantoolTuple.build(type, fields, (List<?>) values))
                .collect(Collectors.toList());
    }
}
