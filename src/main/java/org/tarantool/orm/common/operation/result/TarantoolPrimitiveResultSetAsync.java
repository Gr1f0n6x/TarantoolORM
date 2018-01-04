package org.tarantool.orm.common.operation.result;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by GrIfOn on 04.01.2018.
 */
final public class TarantoolPrimitiveResultSetAsync<T> implements TarantoolResultSet<T> {
    private Future<List<?>> resultSet;

    public TarantoolPrimitiveResultSetAsync(Future<List<?>> resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public List<T> get() {
        try {
            return resultSet.get().stream()
                    .map(value -> (T) value)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
