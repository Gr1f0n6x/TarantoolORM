package org.tarantool.internals.operations;

import org.tarantool.TarantoolClient;
import org.tarantool.internals.Meta;

import java.util.List;
import java.util.concurrent.CompletionStage;

public final class DeleteOperation<T> implements Operation<T> {
    private final TarantoolClient tarantoolClient;
    private final Meta<T> meta;
    private final String spaceName;
    private final List<?> keys;

    public DeleteOperation(TarantoolClient tarantoolClient, Meta<T> meta, String spaceName, List<?> keys) {
        this.tarantoolClient = tarantoolClient;
        this.meta = meta;
        this.spaceName = spaceName;
        this.keys = keys;
    }

    @Override
    public T runSync() {
        List<?> result = tarantoolClient.syncOps().delete(spaceName, keys);
        return meta.resultToDataClass(result);
    }

    @Override
    public CompletionStage<T> runAsync() {
        return tarantoolClient.composableAsyncOps().delete(spaceName, keys)
                .thenApply(meta::resultToDataClass);
    }
}
