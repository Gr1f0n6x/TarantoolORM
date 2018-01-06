import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tarantool.TarantoolClient;
import org.tarantool.orm.TarantoolORMClient;
import org.tarantool.orm.TarantoolSchema;
import org.tarantool.orm.common.exception.TarantoolIndexNullPointerException;
import org.tarantool.orm.common.exception.TarantoolORMException;
import org.tarantool.orm.common.type.IndexType;
import org.tarantool.orm.space.TarantoolSpace;

import static org.junit.Assert.*;


/**
 * Created by GrIfOn on 06.01.2018.
 */
public class AsyncIndexTest {
    private TarantoolClient client;
    private TarantoolSpace<MyTuple> space;

    @Before
    public void initClient() throws TarantoolORMException {
        this.client = TarantoolORMClient.build("192.168.99.100", 3301);
        this.space = TarantoolSchema.createSpaceAsync(client, MyTuple.class, "my_space", true);
    }

    @Test
    public void testMinMax() throws TarantoolIndexNullPointerException {
        MyTuple tuple_1 = new MyTuple(1, 1L, "A", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple tuple_2 = new MyTuple(2, 2L, "B", new Integer[] {1,2,3,4}, "value", 1, 1);
        try {
            space.insert(tuple_1);
            space.insert(tuple_2);
            assertEquals("[MyTuple{f=1, a=1, b=1, c=A, d=[1, 2, 3, 4], e=value, g=1}]", space.index(false).min().get().toString());
            assertEquals("[MyTuple{f=1, a=2, b=2, c=B, d=[1, 2, 3, 4], e=value, g=1}]", space.index(false).max().get().toString());
        } finally {
            space.delete(tuple_1, true);
            space.delete(tuple_2, true);
        }
    }

    @Test
    public void testCount() throws TarantoolIndexNullPointerException {
        MyTuple tuple_1 = new MyTuple(3, 3L, "A", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple tuple_2 = new MyTuple(4, 4L, "A", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple tuple_3 = new MyTuple(5, 4L, "B", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple tuple_4 = new MyTuple(6, 4L, "B", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple tuple_5 = new MyTuple(7, 4L, "C", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple tuple_6 = new MyTuple(8, 4L, "C", new Integer[] {1,2,3,4}, "value", 1, 1);
        try {
            space.insert(tuple_1);
            space.insert(tuple_2);
            space.insert(tuple_3);
            space.insert(tuple_4);
            space.insert(tuple_5);
            space.insert(tuple_6);
            assertEquals("[1]", space.index(true).count(tuple_1).get().toString());
        } finally {
            space.delete(tuple_1, true);
            space.delete(tuple_2, true);
            space.delete(tuple_3, true);
            space.delete(tuple_4, true);
            space.delete(tuple_5, true);
            space.delete(tuple_6, true);
        }
    }

    @Test
    public void testCountSecondaryIndex() throws TarantoolIndexNullPointerException {
        MyTuple tuple_1 = new MyTuple(5, 5L, "A", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple tuple_2 = new MyTuple(6, 6L, "A", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple tuple_3 = new MyTuple(7, 6L, "B", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple tuple_4 = new MyTuple(8, 6L, "B", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple tuple_5 = new MyTuple(9, 6L, "C", new Integer[] {1,2,3,4}, "value", 1, 1);
        MyTuple tuple_6 = new MyTuple(10, 6L, "C", new Integer[] {1,2,3,4}, "value", 1, 1);
        try {
            space.insert(tuple_1);
            space.insert(tuple_2);
            space.insert(tuple_3);
            space.insert(tuple_4);
            space.insert(tuple_5);
            space.insert(tuple_6);
            assertEquals("[2]", space.index(false).count(tuple_1).get().toString());
        } finally {
            space.delete(tuple_1, true);
            space.delete(tuple_2, true);
            space.delete(tuple_3, true);
            space.delete(tuple_4, true);
            space.delete(tuple_5, true);
            space.delete(tuple_6, true);
        }
    }

    @After
    public void testAlter() throws TarantoolIndexNullPointerException {
        space.index(false).alter(true, IndexType.HASH);
        space.index(false).drop();
    }
}
