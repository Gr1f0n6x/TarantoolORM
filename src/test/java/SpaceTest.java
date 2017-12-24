import org.tarantool.orm.TarantoolIndex;
import org.tarantool.orm.exception.TarantoolNoSuchIndexException;
import org.tarantool.orm.exception.TarantoolORMException;
import org.junit.Before;
import org.junit.Test;
import org.tarantool.SocketChannelProvider;
import org.tarantool.TarantoolClient;
import org.tarantool.TarantoolClientConfig;
import org.tarantool.TarantoolClientImpl;
import org.tarantool.orm.TarantoolSpace;
import org.tarantool.orm.type.IndexType;
import org.tarantool.orm.type.IteratorType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * Created by GrIfOn on 20.12.2017.
 */

// docker run --rm -p 3301:3301 -t -i tarantool/tarantool:1.7
public class SpaceTest {
    private TarantoolClient client;
    private TarantoolSpace<MyTuple> space;

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
        this.space = new TarantoolSpace(client, MyTuple.class, "my_space", true);

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
            assertEquals("[[1, 1, insert]]", space.insert(tuple).toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testAsyncInsert() throws ExecutionException, InterruptedException, TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(2, 2L, "asyncInsert");
        try {
            assertEquals("[[2, 2, asyncInsert]]", space.asyncInsert(tuple).get().toString());
        } finally {
            space.asyncDelete(tuple, true);
        }
    }

    @Test
    public void testSelect() throws TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(3, 3L, "select");
        try {
            space.insert(tuple);
            assertEquals("[[3, 3, select]]", space.select(tuple, true, 0, 100, IteratorType.EQ).toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testAsyncSelect() throws ExecutionException, InterruptedException, TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(4, 4L, "asyncSelect");
        try {
            space.insert(tuple);
            assertEquals("[[4, 4, asyncSelect]]", space.asyncSelect(tuple, true, 0, 100, IteratorType.EQ).get().toString());
        } finally {
            space.asyncDelete(tuple, true);
        }
    }

    @Test
    public void testDelete() throws TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(5, 5L, "delete");
        space.insert(tuple);
        assertEquals("[[5, 5, delete]]", space.delete(tuple, true).toString());
    }

    @Test
    public void testAsyncDelete() throws ExecutionException, InterruptedException, TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(6, 6L, "asyncDelete");
        space.insert(tuple);
        assertEquals("[[6, 6, asyncDelete]]", space.asyncDelete(tuple, true).get().toString());
    }

    @Test
    public void testUpdate() throws TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(7, 7L, "before");
        MyTuple update = new MyTuple(7, 7L, "update");
        try {
            space.insert(tuple);
            assertEquals("[[7, 7, update]]", space.update(update, true).toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testAsyncUpdate() throws ExecutionException, InterruptedException, TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(8, 8L, "before");
        MyTuple asyncUpdate = new MyTuple(8, 8L, "asyncUpdate");
        try {
            space.insert(tuple);
            assertEquals("[[8, 8, asyncUpdate]]", space.asyncUpdate(asyncUpdate, true).get().toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testReplace() throws TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(9, 9L, "before");
        MyTuple replace = new MyTuple(9, 9L, "replace");
        try {
            space.insert(tuple);
            assertEquals("[[9, 9, replace]]", space.replace(replace).toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testAsyncReplace() throws ExecutionException, InterruptedException, TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(10, 10L, "before");
        MyTuple asyncReplace = new MyTuple(10, 10L, "asyncReplace");
        try {
            space.insert(tuple);
            assertEquals("[[10, 10, asyncReplace]]", space.asyncReplace(asyncReplace).get().toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testUpsert() throws TarantoolNoSuchIndexException {
        MyTuple tuple = new MyTuple(11, 11L, "before");
        MyTuple upsert = new MyTuple(11, 11L, "upsert");
        try {
            space.insert(tuple);
            space.upsert(upsert, true);
            assertEquals("[[11, 11, upsert]]", space.select(upsert, true, 0, 1, IteratorType.EQ).toString());
        } finally {
            space.delete(upsert, true);
        }
    }

    @Test
    public void testAsyncUpsert() throws TarantoolNoSuchIndexException, ExecutionException, InterruptedException {
        MyTuple tuple = new MyTuple(12, 12L, "before");
        MyTuple asyncUpsert = new MyTuple(12, 12L, "asyncUpsert");
        try {
            space.asyncInsert(tuple);
            space.asyncUpsert(asyncUpsert, true);
            assertEquals("[[12, 12, asyncUpsert]]", space.asyncSelect(asyncUpsert, true, 0, 1, IteratorType.EQ).get().toString());
        } finally {
            space.asyncDelete(asyncUpsert, true);
        }
    }
}
