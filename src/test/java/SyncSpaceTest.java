import org.junit.Before;
import org.junit.Test;
import org.tarantool.SocketChannelProvider;
import org.tarantool.TarantoolClient;
import org.tarantool.TarantoolClientConfig;
import org.tarantool.TarantoolClientImpl;
import org.tarantool.orm.TarantoolIndex;
import org.tarantool.orm.TarantoolSchema;
import org.tarantool.orm.TarantoolSpace;
import org.tarantool.orm.TarantoolSpaceSyncOps;
import org.tarantool.orm.exception.TarantoolNoSuchIndexException;
import org.tarantool.orm.exception.TarantoolORMException;
import org.tarantool.orm.type.IndexType;
import org.tarantool.orm.type.IteratorType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import static org.junit.Assert.assertEquals;

/**
 * Created by GrIfOn on 29.12.2017.
 */
public class SyncSpaceTest {
    private TarantoolClient client;
    private TarantoolSpaceSyncOps<MyTuple> space;

    @Before
    public void initClient() throws TarantoolORMException {
        TarantoolClientConfig config = new TarantoolClientConfig();
        config.username = "guest";

        SocketChannelProvider socketChannelProvider = (retryNumber, lastError) -> {
            if (lastError != null) {
                lastError.printStackTrace(System.out);
            }
            try {
                return SocketChannel.open(new InetSocketAddress("192.168.99.100", 3301));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        };

        this.client = new TarantoolClientImpl(socketChannelProvider, config);
        this.space = ((TarantoolSpaceSyncOps<MyTuple>)TarantoolSchema.createSpace(client, MyTuple.class, "my_space", true));

        TarantoolIndex primary = new TarantoolIndex("primary", IndexType.HASH, true);
        TarantoolIndex secondary = new TarantoolIndex("secondary", IndexType.TREE, true, false);

        space.createIndex(primary, true);
        space.createIndex(secondary, false);
    }

    @Test
    public void testTupleIndexFields() {
        assertEquals("{secondary=[@org.tarantool.orm.annotation.IndexField(type=scalar, part=3, indexName=secondary)], primary=[@org.tarantool.orm.annotation.IndexField(type=integer, part=1, indexName=primary), @org.tarantool.orm.annotation.IndexField(type=unsigned, part=2, indexName=primary)]}", space.getIndexFields().toString());
    }

    @Test
    public void testCreatePrimaryIndex() {
        assertEquals(0, space.getPrimaryIndexId());
    }

    @Test
    public void testCreateSecondaryIndex() {
        assertEquals(1, space.getSecondaryIndexId());
    }

    @Test
    public void testInsert() throws TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(1, 1L, "insert");
        try {
            assertEquals("[Tuple{[1, 1, insert]}]", space.insert(tuple).get().toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testSelect() throws TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(2, 2L, "select");
        try {
            space.insert(tuple);
            assertEquals("[Tuple{[2, 2, select]}]", space.select(tuple, true, 0, 100, IteratorType.EQ).get().toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testDelete() throws TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(3, 3L, "delete");
        space.insert(tuple);
        assertEquals("[Tuple{[3, 3, delete]}]", space.delete(tuple, true).get().toString());
    }

    @Test
    public void testUpdate() throws TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(4, 4L, "before");
        MyTuple update = new MyTuple(4, 4L, "update");
        try {
            space.insert(tuple);
            assertEquals("[Tuple{[4, 4, update]}]", space.update(update, true).get().toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testReplace() throws TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(5, 5L, "before");
        MyTuple replace = new MyTuple(5, 5L, "replace");
        try {
            space.insert(tuple);
            assertEquals("[Tuple{[5, 5, replace]}]", space.replace(replace).get().toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testUpsert() throws TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(6, 6L, "before");
        MyTuple upsert = new MyTuple(6, 6L, "upsert");
        try {
            space.insert(tuple);
            space.upsert(upsert, true);
            assertEquals("[Tuple{[6, 6, upsert]}]", space.select(upsert, true, 0, 1, IteratorType.EQ).get().toString());
        } finally {
            space.delete(upsert, true);
        }
    }

    @Test
    public void testMinMax() throws TarantoolNoSuchIndexException {
        MyTuple tuple_1 = new MyTuple(7, 7L, "A");
        MyTuple tuple_2 = new MyTuple(8, 8L, "B");
        try {
            space.insert(tuple_1);
            space.insert(tuple_2);
            assertEquals("[Tuple{[7, 7, A]}]", space.min(false).get().toString());
            assertEquals("[Tuple{[8, 8, B]}]", space.max(false).get().toString());
        } finally {
            space.delete(tuple_1, true);
            space.delete(tuple_2, true);
        }
    }
}
