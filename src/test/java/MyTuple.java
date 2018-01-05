import org.tarantool.orm.common.annotations.Field;
import org.tarantool.orm.common.annotations.Index;
import org.tarantool.orm.common.annotations.Indexes;
import org.tarantool.orm.common.type.IndexType;
import org.tarantool.orm.entity.TarantoolField;
import org.tarantool.orm.entity.TarantoolTuple;
import org.tarantool.orm.common.annotations.IndexField;
import org.tarantool.orm.common.type.TarantoolType;

import java.util.Map;

/**
 * Created by GrIfOn on 29.12.2017.
 */
@Indexes(indexList = {
        @Index(name = "primary", type = IndexType.HASH, unique = true, ifNotExists = true),
        @Index(name = "secondary", type = IndexType.TREE, ifNotExists = true)
})
public class MyTuple extends TarantoolTuple {
    @Field(position = 6)
    TarantoolField<Integer> f = new TarantoolField<>(100);
    @Field(position = 1)
    @IndexField(indexName = "primary", part = 1, type = TarantoolType.INTEGER)
    TarantoolField<Integer> a = new TarantoolField<>(10);
    @Field(position = 2)
    @IndexField(indexName = "primary", part = 2, type = TarantoolType.UNSIGNED)
    TarantoolField<Long> b = new TarantoolField<>(20L);
    @Field(position = 3)
    @IndexField(indexName = "secondary",  part = 3, type = TarantoolType.SCALAR)
    TarantoolField<String> c = new TarantoolField<>("value");
    @Field(position = 5)
    TarantoolField<Integer[]> d = new TarantoolField<>(new Integer[] {1, 2, 3, 4});
    @Field(position = 4)
    TarantoolField<String> e = new TarantoolField<>("value");
    @Field(position = 7)
    TarantoolField<Integer> g = new TarantoolField<>(100);
    int j = 500;

    public MyTuple() {
    }

    public MyTuple(Integer a, Long b, String c, Integer[] d, String e, Integer f, Integer g) {
        this.a.setValue(a);
        this.b.setValue(b);
        this.c.setValue(c);
        this.d.setValue(d);
        this.e.setValue(e);
        this.f.setValue(f);
        this.g.setValue(g);
    }
}