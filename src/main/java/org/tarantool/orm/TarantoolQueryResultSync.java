package org.tarantool.orm;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by GrIfOn on 29.12.2017.
 */
public class TarantoolQueryResultSync<T extends TarantoolTuple> implements TarantoolQueryResult<T> {
    private List<?> resultSet;
    private Class<T> type;

    public TarantoolQueryResultSync(List<?> resultSet, Class<T> type) {
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
