import org.tarantool.orm.annotation.IndexField;
import org.junit.Before;
import org.junit.Test;
import org.tarantool.orm.type.IndexType;
import org.tarantool.orm.TarantoolIndex;
import org.tarantool.orm.type.IteratorType;
import org.tarantool.orm.type.TarantoolType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by GrIfOn on 21.12.2017.
 */

public class IndexTest {
    class Annotation implements IndexField {
        private String indexName;
        private int part;
        private TarantoolType type;

        public Annotation(String indexName, int part, TarantoolType type) {
            this.indexName = indexName;
            this.part = part;
            this.type = type;
        }

        @Override
        public String indexName() {
            return this.indexName;
        }

        @Override
        public int part() {
            return this.part;
        }

        @Override
        public TarantoolType type() {
            return this.type;
        }

        @Override
        public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return IndexField.class;
        }
    }

    private List<IndexField> indexFields;
    private TarantoolIndex index;

    @Before
    public void init() {
        this.indexFields = Stream.
                of(
                        new Annotation("test", 1, TarantoolType.STRING),
                        new Annotation("test", 2, TarantoolType.INTEGER),
                        new Annotation("test", 3, TarantoolType.SCALAR),
                        new Annotation("test", 4, TarantoolType.BOOLEAN),
                        new Annotation("test", 5, TarantoolType.ARRAY)
                )
                .collect(Collectors.toList());

        this.index = new TarantoolIndex("primary", IndexType.HASH, true);
    }

    @Test
    public void testCreateIndexQuery() {
       String query = index.createIndex("my_space", this.indexFields);

       assertEquals("box.space.my_space:create_index('primary', {type='hash', if_not_exists=true, unique=true, parts={1, 'str', 2, 'integer', 3, 'scalar', 4, 'boolean', 5, 'array'}})", query);
    }

    @Test
    public void testCreateIndexQueryBySpaceId() {
        String query = index.createIndex(1, this.indexFields);

        assertEquals("box.space[1]:create_index('primary', {type='hash', if_not_exists=true, unique=true, parts={1, 'str', 2, 'integer', 3, 'scalar', 4, 'boolean', 5, 'array'}})", query);
    }

    @Test
    public void testDropIndex() {
        String query = index.dropIndex("my_space");

        assertEquals("box.space.my_space.index.primary:drop()", query);
    }

    @Test
    public void testDropIndexBySpaceId() {
        String query = index.dropIndex(1);

        assertEquals("box.space[1].index.primary:drop()", query);
    }

    @Test
    public void testIndexParts() {
        index.createIndex(1, this.indexFields);

        assertEquals("[IndexField{1, str}, IndexField{2, integer}, IndexField{3, scalar}, IndexField{4, boolean}, IndexField{5, array}]", this.index.parts());
    }

    @Test
    public void testMin() {
        String query = index.min("my_space");

        assertEquals("return box.space.my_space.index.primary:min()", query);
    }

    @Test
    public void TestMinBySpaceId() {
        String query = index.min(1);

        assertEquals("return box.space[1].index.primary:min()", query);
    }

    @Test
    public void testMax() {
        String query = index.max("my_space");

        assertEquals("return box.space.my_space.index.primary:max()", query);
    }

    @Test
    public void testMaxBySpaceId() {
        String query = index.max(1);

        assertEquals("return box.space[1].index.primary:max()", query);
    }

    @Test
    public void testRandom() {
        String query = index.random("my_space", 1);

        assertEquals("return box.space.my_space.index.primary:random(1)", query);
    }

    @Test
    public void testRandomBySpaceId() {
        String query = index.random(1, 2);

        assertEquals("return box.space[1].index.primary:random(2)", query);
    }

    @Test
    public void testCount() {
        String query = index.count("my_space", new MyTuple().getValues());

        assertEquals("return box.space.my_space.index.primary:count({10, 20, value})", query);
    }

    @Test
    public void testCountBySpaceId() {
        String query = index.count(1, new MyTuple().getValues());

        assertEquals("return box.space[1].index.primary:count({10, 20, value})", query);
    }

    @Test
    public void testCountIterator() {
        String query = index.count("my_space", new MyTuple().getValues(), IteratorType.GE);

        assertEquals("return box.space.my_space.index.primary:count({10, 20, value}, {iterator = 'GE'})", query);
    }

    @Test
    public void testCountBySpaceIdIterator() {
        String query = index.count(1, new MyTuple().getValues(), IteratorType.ALL);

        assertEquals("return box.space[1].index.primary:count({10, 20, value}, {iterator = 'ALL'})", query);
    }

    @Test
    public void testBsize() {
        String query = index.bsize("my_space");

        assertEquals("return box.space.my_space.index.primary:bsize()", query);
    }

    @Test
    public void testBsizeBySpaceId() {
        String query = index.bsize(1);

        assertEquals("return box.space[1].index.primary:bsize()", query);
    }

    @Test
    public void testAlter() {
        String query = index.alter("my_space", false, IndexType.TREE);

        assertEquals("return box.space.my_space.index.primary:alter({type = 'tree', unique = false})", query);
    }

    @Test
    public void testAlterBySpaceId() {
        String query = index.alter(1, true, IndexType.HASH);

        assertEquals("return box.space[1].index.primary:alter({type = 'hash', unique = true})", query);
    }
}
