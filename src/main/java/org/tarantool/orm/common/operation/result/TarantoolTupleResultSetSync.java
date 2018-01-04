package org.tarantool.orm.common.operation.result;

import org.tarantool.orm.entity.TarantoolTuple;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by GrIfOn on 04.01.2018.
 */
final public class TarantoolTupleResultSetSync<T extends TarantoolTuple> implements TarantoolResultSet<T> {
    private List<?> resultSet;
    private Class<T> type;

    public TarantoolTupleResultSetSync(List<?> resultSet, Class<T> type) {
        this.resultSet = resultSet;
        this.type = type;
    }

    @Override
    public List<T> get() {
        return resultSet.stream()
                .map(values -> {
                    try {
                        return (T) TarantoolTuple.build(type, (List<?>) values);
                    } catch (IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }
}
