import org.tarantool.orm.annotation.IndexField;
import org.junit.Test;
import org.tarantool.orm.TarantoolField;
import org.tarantool.orm.TarantoolTuple;
import org.tarantool.orm.type.TarantoolType;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by GrIfOn on 20.12.2017.
 */
class MyTuple extends TarantoolTuple {
    @IndexField(indexName = "primary", part = 1, type = TarantoolType.INTEGER)
    TarantoolField<Integer> a = new TarantoolField<>(10);
    @IndexField(indexName = "primary", part = 2, type = TarantoolType.UNSIGNED)
    TarantoolField<Long> b = new TarantoolField<>(20L);
    @IndexField(indexName = "secondary",  part = 3, type = TarantoolType.SCALAR)
    TarantoolField<String> c = new TarantoolField<>("value");

    public MyTuple() {
    }

    public MyTuple(Integer a, Long b, String c) {
        this.a.setValue(a);
        this.b.setValue(b);
        this.c.setValue(c);
    }
}

public class TupleTest {
    @Test
    public void testTupleFields() {
        TarantoolTuple tuple = new MyTuple();
        assertEquals("Tuple{[10, 20, value]}", tuple.toString());
    }

    @Test
    public void testGetValues() {
        TarantoolTuple tuple = new MyTuple();
        assertEquals("[10, 20, value]", tuple.getValues().toString());
    }

    @Test
    public void testGetIndexFieldsSecondary() {
        TarantoolTuple tuple = new MyTuple();
        assertEquals("[value]", tuple.getIndexValues("secondary").toString());
    }

    @Test
    public void testGetIndexFieldsPrimary() {
        TarantoolTuple tuple = new MyTuple();
        assertEquals("[10, 20]", tuple.getIndexValues("primary").toString());
    }

    @Test
    public void testGetIndexFieldsNull() {
        TarantoolTuple tuple = new MyTuple();
        assertEquals("[]", tuple.getIndexValues("third").toString());
    }

    @Test
    public void testFieldCount() {
        TarantoolTuple tuple = new MyTuple();
        assertEquals(3, tuple.getFieldCount());
    }

    @Test
    public void testGetValuesForUpdate() {
        TarantoolTuple tuple = new MyTuple();
        assertEquals("[[=, 0, 10], [=, 1, 20], [=, 2, value]]", Arrays.toString(tuple.getValuesForUpdate()));
    }
}
