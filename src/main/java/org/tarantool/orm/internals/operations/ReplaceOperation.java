package org.tarantool.orm.internals.operations;

import org.tarantool.TarantoolClient;
import org.tarantool.orm.internals.Meta;

import java.util.List;
import java.util.concurrent.CompletionStage;

public final class ReplaceOperation<T> implements Operation<T> {
    private final TarantoolClient tarantoolClient;
    private final Meta<T> meta;
    private final String spaceName;
    private final T value;

    public ReplaceOperation(TarantoolClient tarantoolClient, Meta<T> meta, String spaceName, T value) {
        this.tarantoolClient = tarantoolClient;
        this.meta = meta;
        this.spaceName = spaceName;
        this.value = value;
    }

    @Override
    public T runSync() {
        List<?> result = tarantoolClient.syncOps().replace(spaceName, meta.toList(value));
        return meta.resultToDataClass(result);
    }

    @Override
    public CompletionStage<T> runAsync() {
        return tarantoolClient.composableAsyncOps().replace(spaceName, meta.toList(value))
                .thenApply(meta::resultToDataClass);
    }
}
