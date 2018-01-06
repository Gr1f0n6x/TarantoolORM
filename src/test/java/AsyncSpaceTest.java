import org.junit.Before;
import org.junit.Test;
import org.tarantool.TarantoolClient;
import org.tarantool.orm.TarantoolORMClient;
import org.tarantool.orm.TarantoolSchema;
import org.tarantool.orm.space.TarantoolSpace;
import org.tarantool.orm.common.exception.TarantoolIndexNullPointerException;
import org.tarantool.orm.common.exception.TarantoolORMException;
import org.tarantool.orm.common.type.IteratorType;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * Created by GrIfOn on 29.12.2017.
 */
public class AsyncSpaceTest {
    private TarantoolSpace<MyTuple> space;

    @Before
    public void initClient() throws TarantoolORMException {
        TarantoolClient client = TarantoolORMClient.build("192.168.99.100", 3301);
        this.space = TarantoolSchema.createSpaceAsync(client, MyTuple.class, "my_space", true);
    }

    @Test
    public void testTupleIndexFields() {
        assertEquals("{secondary=[@org.tarantool.orm.common.annotations.IndexField(type=str, part=3, indexName=secondary)], primary=[@org.tarantool.orm.common.annotations.IndexField(type=integer, part=1, indexName=primary), @org.tarantool.orm.common.annotations.IndexField(type=unsigned, part=2, indexName=primary)]}", space.getIndexFields().toString());
    }

    @Test
    public void testIndexId() {
        assertEquals(0, space.index(true).getIndexId());
        assertEquals(1, space.index(false).getIndexId());
    }

    @Test
    public void testInsert() throws TarantoolIndexNullPointerException, ExecutionException, InterruptedException {
        MyTuple tuple = new MyTuple(1, 1L, "insert", new Integer[] {1,2,3,4}, "value", 1, 1);
        try {
            assertEquals("[MyTuple{f=1, a=1, b=1, c=insert, d=[1, 2, 3, 4], e=value, g=1}]", space.insert(tuple).get().toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testSelect() throws TarantoolIndexNullPointerException, ExecutionException, InterruptedException {
        MyTuple tuple = new MyTuple(2, 2L, "select", new Integer[] {1,2,3,4}, "value", 1, 1);
        try {
            space.insert(tuple);
            assertEquals("[MyTuple{f=1, a=2, b=2, c=select, d=[1, 2, 3, 4], e=value, g=1}]", space.select(tuple, true, 0, 100, IteratorType.EQ).get().toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testSelectWithNulls() throws TarantoolIndexNullPointerException {
        MyTuple tuple = new MyTuple(2, 2L, "select", new Integer[] {1,2,3,4}, "value", null, 1);
        try {
            space.insert(tuple);
            assertEquals("[MyTuple{f=null, a=2, b=2, c=select, d=[1, 2, 3, 4], e=value, g=1}]", space.select(tuple, true, 0, 100, IteratorType.EQ).get().toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testDelete() throws TarantoolIndexNullPointerException, ExecutionException, InterruptedException {
        MyTuple tuple = new MyTuple(3, 3L, "delete", new Integer[] {1,2,3,4}, "value", 1, 1);
        space.insert(tuple);
        assertEquals("[MyTuple{f=1, a=3, b=3, c=delete, d=[1, 2, 3, 4], e=value, g=1}]", space.delete(tuple, true).get().toString());
    }

    @Test
    public void testUpdate() throws TarantoolIndexNullPointerException, ExecutionException, InterruptedException {
        MyTuple tuple = new MyTuple(4, 4L, "before", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple update = new MyTuple(4, 4L, "update", new Integer[] {1,2,3,4}, "value", 1, 1);
        try {
            space.insert(tuple);
            assertEquals("[MyTuple{f=1, a=4, b=4, c=update, d=[1, 2, 3, 4], e=value, g=1}]", space.update(update, true).get().toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testReplace() throws TarantoolIndexNullPointerException, ExecutionException, InterruptedException {
        MyTuple tuple = new MyTuple(5, 5L, "before", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple replace = new MyTuple(5, 5L, "replace", new Integer[] {1,2,3,4}, "value", 1, 1);
        try {
            space.insert(tuple);
            assertEquals("[MyTuple{f=1, a=5, b=5, c=replace, d=[1, 2, 3, 4], e=value, g=1}]", space.replace(replace).get().toString());
        } finally {
            space.delete(tuple, true);
        }
    }

    @Test
    public void testUpsert() throws TarantoolIndexNullPointerException, ExecutionException, InterruptedException {
        MyTuple tuple = new MyTuple(6, 6L, "before", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple upsert = new MyTuple(6, 6L, "upsert", new Integer[] {1,2,3,4}, "value", 1, 1);
        try {
            space.insert(tuple);
            space.upsert(upsert, true);
            assertEquals("[MyTuple{f=1, a=6, b=6, c=upsert, d=[1, 2, 3, 4], e=value, g=1}]", space.select(upsert, true, 0, 1, IteratorType.EQ).get().toString());
        } finally {
            space.delete(upsert, true);
        }
    }
}
