package org.tarantool.orm;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by GrIfOn on 29.12.2017.
 */
public class TarantoolQueryResultAsync<T extends TarantoolTuple> implements TarantoolQueryResult<T> {
    private Future<List<?>> resultSet;
    private Class<T> type;

    public TarantoolQueryResultAsync(Future<List<?>> resultSet, Class<T> type) {
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
            return Collections.emptyList();
        }
    }
}
