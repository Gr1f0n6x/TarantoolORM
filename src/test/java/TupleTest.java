import org.tarantool.orm.annotation.IndexField;
import org.junit.Test;
import org.tarantool.orm.TarantoolField;
import org.tarantool.orm.TarantoolTuple;
import org.tarantool.orm.type.TarantoolType;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by GrIfOn on 20.12.2017.
 */
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

    @Test
    public void testCostructorWithValueList() throws InstantiationException, IllegalAccessException {
        MyTuple tuple = (MyTuple) TarantoolTuple.build(MyTuple.class, Arrays.asList(1, 2L, "valueFromList"));
        assertEquals("[1, 2, valueFromList]", tuple.getValues().toString());
    }
}
