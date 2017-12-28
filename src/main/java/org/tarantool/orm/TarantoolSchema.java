package org.tarantool.orm;

import org.tarantool.TarantoolClient;
import org.tarantool.orm.exception.TarantoolORMException;

/**
 * Created by GrIfOn on 24.12.2017.
 */
public class TarantoolSchema {
    public static <T extends TarantoolTuple> TarantoolSpace<T> createSpace(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists) throws TarantoolORMException {
        return new TarantoolSpaceSyncOps<>(client, type, spaceName, ifNotExists);
    }

    public static <T extends TarantoolTuple> TarantoolSpace<T> createSpaceAsync(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists) throws TarantoolORMException {
        return new TarantoolSpaceAsyncOps<>(client, type, spaceName, ifNotExists);
    }
}
