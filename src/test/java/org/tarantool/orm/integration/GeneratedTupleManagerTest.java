package org.tarantool.orm.integration;

import org.junit.*;
import org.tarantool.TarantoolClient;
import org.tarantool.TarantoolClientConfig;
import org.tarantool.TarantoolClientImpl;
import org.tarantool.orm.generated.ManagerFactory;
import org.tarantool.orm.generated.MyTupleManager;
import org.testcontainers.containers.GenericContainer;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class GeneratedTupleManagerTest {
    @ClassRule
    public static GenericContainer tarantool = new GenericContainer<>("tarantool/tarantool:2.3")
            .withExposedPorts(3301);

    private static TarantoolClient client;
    private static MyTupleManager manager;

    @BeforeClass
    public static void setUp() {
        TarantoolClientConfig config = new TarantoolClientConfig();
        config.username = "guest";

        String host = tarantool.getContainerIpAddress();
        int port = tarantool.getMappedPort(3301);
        client = new TarantoolClientImpl(String.format("%s:%s", host, port), config);

        client.syncOps().eval("box.schema.space.create('test', { if_not_exists = true })");
        client.syncOps().eval("box.space.test:create_index('primary', {unique = true, if_not_exists = true, parts = { {field = 1, type = 'number'} } })");
        client.syncOps().eval("box.space.test:create_index('secondary', {unique = false, if_not_exists = true, parts = { {field = 2, type = 'string'} } })");

        manager = new ManagerFactory(client).myTupleManager();
    }

    @AfterClass
    public static void cleanUp() {
        client.syncOps().eval("box.space.test:drop()");

        client.close();
    }

    @After
    public void truncate() {
        client.syncOps().eval("box.space.test:truncate()");
    }

    private static MyTuple tuple() {
        MyTuple tuple = new MyTuple();

        tuple.setF1(1);
        tuple.setF2("2");
        tuple.setF3((short) 3);
        tuple.setF4((byte) 4);
        tuple.setF5(5L);
        tuple.setF6(6.0f);
        tuple.setF7(7.0d);
        tuple.setF8(new long[] {1L, 2L, 3L});
        tuple.setF9(true);
        tuple.setF10(new Object[] {1, "second", 3, 4, "fifth"});
        tuple.setF11(Collections.singletonMap("key", "value"));

        return tuple;
    }

    @Test
    public void insertSync() {
        MyTuple tuple = tuple();
        MyTuple result = manager.insert(tuple).runSync();
        assertEquals(tuple, result);
    }

    @Test
    public void insertAsync() throws InterruptedException, ExecutionException, TimeoutException {
        MyTuple tuple = tuple();
        MyTuple result = manager.insert(tuple).runAsync().toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertEquals(tuple, result);
    }

    @Test
    public void selectSync() {
        MyTuple tuple = tuple();
        manager.insert(tuple).runSync();

        MyTuple resultUsingPrimaryIndex = manager.selectUsingPrimaryIndex(1).runSync();
        MyTuple resultUsingSecondaryIndex = manager.selectUsingSecondaryIndex("2").runSync();
        assertEquals(tuple, resultUsingPrimaryIndex);
        assertEquals(tuple, resultUsingSecondaryIndex);
    }

    @Test
    public void selectAsync() throws InterruptedException, ExecutionException, TimeoutException {
        MyTuple tuple = tuple();
        manager.insert(tuple).runSync();

        MyTuple resultUsingPrimaryIndex = manager.selectUsingPrimaryIndex(1).runAsync().toCompletableFuture().get(5, TimeUnit.SECONDS);
        MyTuple resultUsingSecondaryIndex = manager.selectUsingSecondaryIndex("2").runAsync().toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertEquals(tuple, resultUsingPrimaryIndex);
        assertEquals(tuple, resultUsingSecondaryIndex);
    }

    @Test
    public void updateSync() {
        MyTuple tuple = tuple();
        manager.insert(tuple).runSync();

        MyTuple updated = tuple();
        updated.setF5(100L);

        MyTuple result = manager.update(updated).runSync();
        assertEquals(updated, result);
        assertNotEquals(tuple, result);
    }

    @Test
    public void updateAsync() throws InterruptedException, ExecutionException, TimeoutException {
        MyTuple tuple = tuple();
        manager.insert(tuple).runSync();

        MyTuple updated = tuple();
        updated.setF5(100L);

        MyTuple result = manager.update(updated).runAsync().toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertEquals(updated, result);
        assertNotEquals(tuple, result);
    }

    @Test
    public void upsertSync() {
        MyTuple defaultValue = tuple();
        defaultValue.setF5(50L);

        MyTuple updated = tuple();
        updated.setF5(100L);

        manager.upsert(defaultValue, updated).runSync();
        MyTuple result = manager.selectUsingPrimaryIndex(1).runSync();
        assertEquals(defaultValue, result);
    }

    @Test
    public void upsertAsync() throws InterruptedException, ExecutionException, TimeoutException {
        MyTuple defaultValue = tuple();
        defaultValue.setF5(50L);

        MyTuple updated = tuple();
        updated.setF5(100L);

        manager.upsert(defaultValue, updated).runAsync().toCompletableFuture().get(5, TimeUnit.SECONDS);
        MyTuple result = manager.selectUsingPrimaryIndex(1).runSync();
        assertEquals(defaultValue, result);
    }

    @Test
    public void replaceSync() {
        MyTuple tuple = tuple();
        manager.insert(tuple).runSync();

        MyTuple newTuple = tuple();
        newTuple.setF3((short) 123);

        MyTuple result = manager.replace(newTuple).runSync();
        assertEquals(newTuple, result);
    }

    @Test
    public void replaceAsync() throws InterruptedException, ExecutionException, TimeoutException {
        MyTuple tuple = tuple();
        manager.insert(tuple).runSync();

        MyTuple newTuple = tuple();
        newTuple.setF3((short) 123);

        MyTuple result = manager.replace(newTuple).runAsync().toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertEquals(newTuple, result);
    }

    @Test
    public void deleteSync() {
        MyTuple tuple = tuple();
        manager.insert(tuple).runSync();

        MyTuple resultUsingPrimaryIndex = manager.selectUsingPrimaryIndex(1).runSync();
        assertEquals(tuple, resultUsingPrimaryIndex);

        manager.delete(tuple).runSync();

        assertNull(manager.selectUsingPrimaryIndex(1).runSync());
    }

    @Test
    public void deleteAsync() throws InterruptedException, ExecutionException, TimeoutException {
        MyTuple tuple = tuple();
        manager.insert(tuple).runSync();

        MyTuple resultUsingPrimaryIndex = manager.selectUsingPrimaryIndex(1).runSync();
        assertEquals(tuple, resultUsingPrimaryIndex);

        manager.delete(tuple).runAsync().toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertNull(manager.selectUsingPrimaryIndex(1).runSync());
    }
}
