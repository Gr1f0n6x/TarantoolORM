package org.tarantool.internals.operations;

import org.tarantool.TarantoolClient;
import org.tarantool.internals.Meta;

import java.util.List;
import java.util.concurrent.CompletionStage;

public final class UpsertOperation<T> implements Operation<T> {
    private final TarantoolClient tarantoolClient;
    private final Meta<T> meta;
    private final String spaceName;
    private final List<?> keys;
    private final T defaultValue;
    private final List<?> ops;

    public UpsertOperation(TarantoolClient tarantoolClient, Meta<T> meta, String spaceName, List<?> keys, T defaultValue, List<?> ops) {
        this.tarantoolClient = tarantoolClient;
        this.meta = meta;
        this.spaceName = spaceName;
        this.keys = keys;
        this.defaultValue = defaultValue;
        this.ops = ops;
    }

    @Override
    public T runSync() {
        List<?> result = tarantoolClient.syncOps().upsert(spaceName, keys, meta.toList(defaultValue), ops.toArray());
        return meta.resultToDataClass(result);
    }

    @Override
    public CompletionStage<T> runAsync() {
        return tarantoolClient.composableAsyncOps().upsert(spaceName, keys, meta.toList(defaultValue), ops.toArray())
                .thenApply(meta::resultToDataClass);
    }
}
