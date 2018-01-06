import org.junit.Before;
import org.junit.Test;
import org.tarantool.orm.entity.TarantoolField;
import org.tarantool.orm.entity.TarantoolTuple;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by GrIfOn on 20.12.2017.
 */
public class TupleTest {
    private Map<Integer, String> fields;

    @Before
    public void setFields() {
        fields = TarantoolTuple.retrieveFieldMap(MyTuple.class);
    }

    @Test
    public void testGetTupleFields() {
        assertEquals("{1=a, 2=b, 3=c, 4=e, 5=d, 6=f, 7=g}", fields.toString());
    }

    @Test
    public void testGetValues() throws NoSuchFieldException, IllegalAccessException {
        TarantoolTuple tuple = new MyTuple();
        assertEquals("[10, 20, value, value, {1, 2, 3, 4}, 100, 100]", tuple.getValues(fields).stream().map(x -> {
            if(x.getClass().isArray()) {
                return Arrays.toString((Object[]) x)
                        .replace('[', '{')
                        .replace(']', '}');
            }
            return x.toString();
        }).collect(Collectors.toList()).toString());
    }

    @Test
    public void testGetIndexFieldsSecondary() {
        TarantoolTuple tuple = new MyTuple();
        assertEquals("[value]", tuple.getIndexValues(fields, "secondary").toString());
    }

    @Test
    public void testGetIndexFieldsPrimary() {
        TarantoolTuple tuple = new MyTuple();
        assertEquals("[10, 20]", tuple.getIndexValues(fields, "primary").toString());
    }

    @Test
    public void testGetIndexFieldsNull() {
        TarantoolTuple tuple = new MyTuple();
        assertEquals("[]", tuple.getIndexValues(fields, "third").toString());
    }

    @Test
    public void testCostructorWithValueList() throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        MyTuple tuple = (MyTuple) TarantoolTuple.build(MyTuple.class, fields, Arrays.asList(1, 2L, "valueFromList", "newValue", new Integer[] {4,3,2,1}, 200, 200));
        assertEquals("[1, 2, valueFromList, newValue, {4, 3, 2, 1}, 200, 200]", tuple.getValues(fields).stream().map(x -> {
            if(x.getClass().isArray()) {
                return Arrays.toString((Object[]) x)
                        .replace('[', '{')
                        .replace(']', '}');
            }
            return x.toString();
        }).collect(Collectors.toList()).toString());
    }
}
