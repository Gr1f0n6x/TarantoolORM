package org.tarantool.internals.operations;

import org.tarantool.TarantoolClient;
import org.tarantool.internals.Meta;

import java.util.List;
import java.util.concurrent.CompletionStage;

public final class UpdateOperation<T> implements Operation<T> {
    private final TarantoolClient tarantoolClient;
    private final Meta<T> meta;
    private final String spaceName;
    private final List<?> keys;
    private final List<?> ops;

    public UpdateOperation(TarantoolClient tarantoolClient, Meta<T> meta, String spaceName, List<?> keys, List<?> ops) {
        this.tarantoolClient = tarantoolClient;
        this.meta = meta;
        this.spaceName = spaceName;
        this.keys = keys;
        this.ops = ops;
    }

    @Override
    public T runSync() {
        List<?> result = tarantoolClient.syncOps().update(spaceName, keys, ops.toArray());
        return meta.resultToDataClass(result);
    }

    @Override
    public CompletionStage<T> runAsync() {
        return tarantoolClient.composableAsyncOps().update(spaceName, keys, ops.toArray())
                .thenApply(meta::resultToDataClass);
    }
}
