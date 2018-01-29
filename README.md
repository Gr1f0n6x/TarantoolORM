# TarantoolORM

It is the wrapper for the [TarantoolConnector](https://github.com/tarantool/tarantool-java)

## Requirements
* Java 1.8 or higher
* Tarantool 1.7.6

## Getting Started

1. Add a dependency to your `pom.xml` file.

```xml
        <dependency>
            <groupId>com.github.Gr1f0n6x</groupId>
            <artifactId>tarantoolorm</artifactId>
            <version>0.4</version>
        </dependency>
```

2. Create a sublcass of `TarantoolTuple`. Specidy index list, fields and index parts using annotations 

```java
@Indexes(indexList = {
        @Index(name = "primary", type = IndexType.HASH, unique = true, ifNotExists = true),
        @Index(name = "secondary", type = IndexType.TREE, ifNotExists = true),
        @Index(name = "third", type = IndexType.TREE, ifNotExists = true)
})
public class Tuple extends TarantoolTuple {
    @Field(position = 6)
    TarantoolField<Integer> f = new TarantoolField<>(100);
    @Field(position = 1)
    @IndexField(params = {@IndexFieldParams(indexName = "primary")}, part = 1, type = TarantoolType.INTEGER)
    TarantoolField<Integer> a = new TarantoolField<>(10);
    @Field(position = 2)
    @IndexField(params = {
            @IndexFieldParams(indexName = "primary"),
            @IndexFieldParams(indexName = "third")
    }, part = 2, type = TarantoolType.UNSIGNED)
    TarantoolField<Long> b = new TarantoolField<>(20L);
    @Field(position = 3)
    @IndexField(params = {
            @IndexFieldParams(indexName = "secondary"),
            @IndexFieldParams(indexName = "third")
    }, part = 3, type = TarantoolType.STRING, collationType = CollationType.UNICODE)
    TarantoolField<String> c = new TarantoolField<>("value");
    @Field(position = 5)
    TarantoolField<Integer[]> d = new TarantoolField<>(new Integer[] {1, 2, 3, 4});
    @Field(position = 4)
    @IndexField(params = {
            @IndexFieldParams(indexName = "third", isNullable = true)
    }, part = 4, type = TarantoolType.STRING)
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
```

3. Create `TarantoolClient` using one of the  `TarantoolORMClient.build()` implementation:


```java
TarantoolClient client = TarantoolORMClient.build("host", port);
TarantoolClient client = TarantoolORMClient.build("username", "password", "host", port);
TarantoolClient client = TarantoolORMClient.build(SocketChannelProvider socketProvider, TarantoolClientConfig config);
```

4. Create a `TarantoolSpace`.

```java
TarantoolSpace space = TarantoolSchema.createSpace(client, Tuple.class, "my_space", true);
TarantoolSpace space = TarantoolSchema.createSpaceAsync(client, Tuple.class, "my_space", true);
```

5. Use
```java
Tuple tuple = new Tuple(2, 2L, "select", new Integer[] {1,2,3,4}, "value", 1, 1);
space.insert(tuple);

List<Tuple> result = space.select(tuple, "primary", 0, 100, IteratorType.EQ).get();

Tuple update = new Tuple(2, 2L, "update", new Integer[] {1,2,3,4}, "update", 2, 2);
space.update(update, "primary");
Tuple replace = new Tuple(2, 2L, "replace", new Integer[] {1,2,3,4}, "value", 1, 1);
space.replace(tuple, "primary");
Tuple upsert = new Tuple(2, 2L, "upsert", new Integer[] {1,2,3,4}, "value", 1, 1);
space.upsert(upsert, "primary");
space.index("secondary").min();
space.delete(tuple, "primary");
```

Each operation depends on index. To specify particular index just pass the index name.

Result type of all operations is TarantoolResultSet<T>. To get values from ResultSet use `get()` method.
This interface is equal for both implementations: `sync` and `async`.

To get access to the index use `space.index(String indexNamw)`. 

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
