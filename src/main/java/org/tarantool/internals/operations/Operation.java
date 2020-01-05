package org.tarantool.internals.operations;

import java.util.concurrent.CompletionStage;

public interface Operation<T> {
    T runSync();

    CompletionStage<T> runAsync();
}
