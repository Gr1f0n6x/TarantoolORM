package org.tarantool.orm.internals.operations;

import org.tarantool.Iterator;
import org.tarantool.TarantoolClient;
import org.tarantool.orm.internals.Meta;

import java.util.List;
import java.util.concurrent.CompletionStage;

public final class SelectOperation<T> implements Operation<T> {
    private final TarantoolClient tarantoolClient;
    private final Meta<T> meta;
    private final String spaceName;
    private final String indexName;
    private final List<?> keys;

    public SelectOperation(TarantoolClient tarantoolClient, Meta<T> meta, String spaceName, String indexName, List<?> keys) {
        this.tarantoolClient = tarantoolClient;
        this.meta = meta;
        this.spaceName = spaceName;
        this.indexName = indexName;
        this.keys = keys;
    }

    @Override
    public T runSync() {
        List<?> result = tarantoolClient.syncOps().select(spaceName, indexName, keys, 0, 1, Iterator.EQ);
        return meta.resultToDataClass(result);
    }

    @Override
    public CompletionStage<T> runAsync() {
        return tarantoolClient.composableAsyncOps().select(spaceName, indexName, keys, 0, 1, Iterator.EQ)
                .thenApply(meta::resultToDataClass);
    }
}
