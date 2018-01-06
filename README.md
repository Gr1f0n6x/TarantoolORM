# TarantoolORM

It is the wrapper for the [TarantoolConnector](https://github.com/tarantool/tarantool-java)

## Getting Started

1. Add a dependency to your `pom.xml` file.

```xml
        <dependency>
            <groupId>com.github.Gr1f0n6x</groupId>
            <artifactId>tarantoolorm</artifactId>
            <version>0.3.9.2</version>
        </dependency>
```

2. Create a sublcass of `TarantoolTuple`. Specidy index list, fields and index parts using annotations 

```java
@Indexes(indexList = {
        @Index(name = "primary", type = IndexType.HASH, unique = true, ifNotExists = true),
        @Index(name = "secondary", type = IndexType.TREE, ifNotExists = true, collationType = CollationType.UNICODE)
})
public class Tuple extends TarantoolTuple {
    @Field(position = 6)
    TarantoolField<Integer> f = new TarantoolField<>();
    @Field(position = 1)
    @IndexField(indexName = "primary", part = 1, type = TarantoolType.INTEGER)
    TarantoolField<Integer> a = new TarantoolField<>();
    @Field(position = 2)
    @IndexField(indexName = "primary", part = 2, type = TarantoolType.UNSIGNED)
    TarantoolField<Long> b = new TarantoolField<>();
    @Field(position = 3)
    @IndexField(indexName = "secondary",  part = 3, type = TarantoolType.STRING)
    TarantoolField<String> c = new TarantoolField<>();
    @Field(position = 5)
    TarantoolField<Integer[]> d = new TarantoolField<>();
    @Field(position = 4)
    TarantoolField<String> e = new TarantoolField<>();
    @Field(position = 7)
    TarantoolField<Integer> g = new TarantoolField<>();
    
    public Tuple(Integer a, Long b, String c, Integer[] d, String e, Integer f, Integer g) {
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

List<Tuple> result = space.select(tuple, true, 0, 100, IteratorType.EQ).get();

Tuple update = new Tuple(2, 2L, "update", new Integer[] {1,2,3,4}, "update", 2, 2);
space.update(update, true);
Tuple replace = new Tuple(2, 2L, "replace", new Integer[] {1,2,3,4}, "value", 1, 1);
space.replace(tuple, true);
Tuple upsert = new Tuple(2, 2L, "upsert", new Integer[] {1,2,3,4}, "value", 1, 1);
space.upsert(upsert, true);
space.index(true).min();
space.delete(tuple, true);
```

Each operation depends on index - if `true` -> primary index, else -> secondary.

Result type of all operations is TarantoolResultSet<T>. To get values from ResultSet use `get()` method.
This interface is equal for both implementations: `sync` and `async`.

To get access to the index use `space.index(boolean primary)` method where `primary` param is needed to specify which index return. If `true` -> primary, else -> secondary. 

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
