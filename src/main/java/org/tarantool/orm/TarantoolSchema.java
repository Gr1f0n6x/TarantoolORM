package org.tarantool.orm;

import org.tarantool.TarantoolClient;
import org.tarantool.orm.entity.TarantoolTuple;
import org.tarantool.orm.common.exception.TarantoolORMException;
import org.tarantool.orm.space.TarantoolSpace;
import org.tarantool.orm.space.TarantoolSpaceAsync;
import org.tarantool.orm.space.TarantoolSpaceSync;

/**
 * Created by GrIfOn on 24.12.2017.
 */
public class TarantoolSchema {
    public static <T extends TarantoolTuple> TarantoolSpace<T> createSpace(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists) throws TarantoolORMException {
        return new TarantoolSpaceSync<>(client, type, spaceName, ifNotExists);
    }

    public static <T extends TarantoolTuple> TarantoolSpace<T> createSpaceAsync(TarantoolClient client, Class<T> type, String spaceName, boolean ifNotExists) throws TarantoolORMException {
        return new TarantoolSpaceAsync<>(client, type, spaceName, ifNotExists);
    }
}
