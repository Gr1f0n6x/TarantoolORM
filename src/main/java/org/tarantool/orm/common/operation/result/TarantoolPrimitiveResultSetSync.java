package org.tarantool.orm.common.operation.result;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by GrIfOn on 04.01.2018.
 */
final public class TarantoolPrimitiveResultSetSync<T> implements TarantoolResultSet<T> {
    private List<?> resultSet;

    public TarantoolPrimitiveResultSetSync(List<?> resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public List<T> get() {
        return resultSet.stream()
                .map(value -> (T) value)
                .collect(Collectors.toList());
    }
}
