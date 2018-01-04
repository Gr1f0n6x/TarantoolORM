package org.tarantool.orm.common.operation.result;

import org.tarantool.orm.entity.TarantoolTuple;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by GrIfOn on 04.01.2018.
 */
final public class TarantoolTupleResultSetAsync<T extends TarantoolTuple> implements TarantoolResultSet<T> {
    private Future<List<?>> resultSet;
    private Class<T> type;

    public TarantoolTupleResultSetAsync(Future<List<?>> resultSet, Class<T> type) {
        this.resultSet = resultSet;
        this.type = type;
    }

    @Override
    public List<T> get() {
        try {
            return resultSet.get().stream()
                    .map(values -> {
                        try {
                            return (T) TarantoolTuple.build(type, (List<?>) values);
                        } catch (IllegalAccessException | InstantiationException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
