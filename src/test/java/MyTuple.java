import org.tarantool.orm.TarantoolField;
import org.tarantool.orm.TarantoolTuple;
import org.tarantool.orm.annotation.IndexField;
import org.tarantool.orm.type.TarantoolType;

/**
 * Created by GrIfOn on 29.12.2017.
 */
public class MyTuple extends TarantoolTuple {
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