package org.tarantool.internals.operations;

import org.tarantool.TarantoolClient;
import org.tarantool.internals.Meta;

import java.util.List;
import java.util.concurrent.CompletionStage;

public final class InsertOperation<T> implements Operation<T> {
    private final TarantoolClient tarantoolClient;
    private final Meta<T> meta;
    private final String spaceName;
    private final T value;

    public InsertOperation(TarantoolClient tarantoolClient, Meta<T> meta, String spaceName, T value) {
        this.tarantoolClient = tarantoolClient;
        this.meta = meta;
        this.spaceName = spaceName;
        this.value = value;
    }

    @Override
    public T runSync() {
        List<?> result = tarantoolClient.syncOps().insert(spaceName, meta.toList(value));
        return meta.resultToDataClass(result);
    }

    @Override
    public CompletionStage<T> runAsync() {
        return tarantoolClient.composableAsyncOps().insert(spaceName, meta.toList(value))
                .thenApply(meta::resultToDataClass);
    }
}
