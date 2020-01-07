# TarantoolORM

[![Build Status](https://img.shields.io/travis/nryanov/tarantool-orm/master.svg)](https://travis-ci.com/nryanov/tarantool-orm)
[![GitHub license](https://img.shields.io/github/license/nryanov/tarantool-orm)](https://github.com/nryanov/tarantool-orm/blob/master/LICENSE.txt)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.nryanov.tarantool/tarantool-orm/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.nryanov.tarantool/tarantool-orm)

It is the wrapper for the [TarantoolConnector](https://github.com/tarantool/tarantool-java)

## Requirements
* Java 1.8 or higher
* Tarantool 1.7.6 or higher 

## Getting Started

- Add a dependency to your `pom.xml` file.
```xml
        <dependency>
            <groupId>com.nryanov.tarantool</groupId>
            <artifactId>tarantool-orm</artifactId>
            <version>{tarantool-orm.version}</version>
        </dependency>
```
- Create a simple POJO: 

```java
@Tuple(spaceName = "test", indexes = {
        @Index(name = "primary", isPrimary = true),
        @Index(name = "secondary")
})
public class DataClass {
    @IndexedField(indexes = @IndexedFieldParams(indexName = "primary"))
    private int f1;
    @IndexedField(indexes = @IndexedFieldParams(indexName = "secondary"))
    private String f2;

    public int getF1() {
        return f1;
    }

    public void setF1(int f1) {
        this.f1 = f1;
    }

    public String getF2() {
        return f2;
    }

    public void setF2(String f2) {
        this.f2 = f2;
    }
}
```

- Create `ManagerFactory`:
```java
        TarantoolClient client = new TarantoolClientImpl(String.format("%s:%s", host, port), config);
        ManagerFactory manager = new ManagerFactory(client);
```

- Using created `ManagerFactory` create manager for your tuple:
```java
    DataClassManager dataClassManager = manager.dataClassManager();
```
- Use!
```java
    DataClass value = new DataClass();
    value.set...
    
    dataClassManager.insert(value).runSync();
    
    int id = ...;
    DataClass select = dataClassManager.select(id).runSync();
```

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
