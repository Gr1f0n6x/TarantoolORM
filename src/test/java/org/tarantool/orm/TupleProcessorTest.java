package org.tarantool.orm;

import com.google.common.base.Joiner;
import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import org.junit.Test;
import org.tarantool.orm.auto.TupleManagerProcessor;

import javax.tools.JavaFileObject;

import java.util.Arrays;


public class TupleProcessorTest {
    public static final String NEW_LINE = System.getProperty("line.separator");

    private final JavaFileObject managerFactoryOutput = JavaFileObjects.forSourceString(
            "org.tarantool.orm.generated.ManagerFactory",
            Joiner.on(NEW_LINE).join(
                    "package org.tarantool.orm.generated;",
                    "",
                    "import org.tarantool.TarantoolClient",
                    "",
                    "public final class ManagerFactory {",
                    "private final TarantoolClient tarantoolClient;",
                    "public ManagerFactory(TarantoolClient tarantoolClient) {",
                    "this.tarantoolClient = tarantoolClient;",
                    "}",
                    "public DataClassManager dataClassManager() {",
                    "return new DataClassManager(this.tarantoolClient);",
                    "}",
                    "}"
            )
    );

    @Test
    public void noGetterError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\"), spaceName = \"test\")",
                        "public class DataClass {",
                        "private int id;",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Field id in class DataClass has no getter");
    }

    @Test
    public void incorrectGetterReturnTypeError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\"), spaceName = \"test\")",
                        "public class DataClass {",
                        "private int id;",
                        "public long getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Field id in class DataClass has getter with incorrect return type");
    }

    @Test
    public void noSetterError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\"), spaceName = \"test\")",
                        "public class DataClass {",
                        "private int id;",
                        "public int getId() {return id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Field id in class DataClass has no setter");
    }

    @Test
    public void incorrectSetterParameterTypeError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\"), spaceName = \"test\")",
                        "public class DataClass {",
                        "private int id;",
                        "public int getId() {return id;}",
                        "public void setId(long id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Field id in class DataClass has setter with incorrect parameter type");
    }

    @Test
    public void emptyIndexNameError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"\"), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private int id;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Index name should not be empty");
    }

    @Test
    public void noIndexError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = {}, spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private int id;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Tuple DataClass must have at least one index");
    }

    @Test
    public void duplicateIndexError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = {@Index(name = \"primary\", isPrimary = true), @Index(name = \"primary\", isPrimary = true)}, spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private int id;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Index names should be unique");
    }

    @Test
    public void noPrimaryIndexError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\"), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private int id;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Tuple DataClass does not have primary index");
    }

    @Test
    public void interfaceAnnotatedByTupleError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\"), spaceName = \"test\")",
                        "public interface DataClass {",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Only classes may be annotated by Tuple: DataClass");
    }

    @Test
    public void emptySpaceNameError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\"), spaceName = \"\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private int id;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Space name should not be empty");
    }

    @Test
    public void dataClassWithoutFieldsError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\"), spaceName = \"test\")",
                        "public class DataClass {",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Class DataClass has no fields");
    }

    @Test
    public void notPublicDataClassError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\"), spaceName = \"test\")",
                        "class DataClass {",
                        "private int id;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Class DataClass should be public");
    }

    @Test
    public void abstractDataClassError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\"), spaceName = \"test\")",
                        "public abstract class DataClass {",
                        "private int id;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Class DataClass should not be abstract");
    }

    @Test
    public void noIndexedFieldError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "private int id;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Data class must have at least one indexed field");
    }

    @Test
    public void nullableFieldOfPrimaryIndexError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\", isNullable = true))",
                        "private int id;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .failsToCompile()
                .withErrorContaining("Primary index can't have nullable fields");
    }

    @Test
    public void simpleTuple() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.IndexedField;",
                        "import org.tarantool.orm.annotations.IndexedFieldParams;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private int id;",
                        "private int value;",
                        "private Object[] objects;",
                        "private long[] longs;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "public int getValue() {return value;}",
                        "public void setValue(int value) {this.value = value;}",
                        "public Object[] getObjects() {return objects;}",
                        "public void setObjects(Object[] objects) {this.objects = objects;}",
                        "public long[] getLongs() {return longs;}",
                        "public void setLongs(long[] longs) {this.longs = longs;}",
                        "}"
                )
        );

        final JavaFileObject managerOutput = JavaFileObjects.forSourceString(
                "org.tarantool.orm.generated.DataClassManager",
                Joiner.on(NEW_LINE).join(

                        "package org.tarantool.orm.generated;",

"import com.google.common.primitives.Longs;",
"import java.lang.Number;",
"import java.lang.Object;",
"import java.lang.String;",
"import java.util.ArrayList;",
"import java.util.Arrays;",
"import java.util.Collection;",
"import java.util.List;",
"import org.tarantool.TarantoolClient;",
"import org.tarantool.orm.internals.Meta;",
"import org.tarantool.orm.internals.operations.DeleteOperation;",
"import org.tarantool.orm.internals.operations.InsertOperation;",
"import org.tarantool.orm.internals.operations.ReplaceOperation;",
"import org.tarantool.orm.internals.operations.SelectOperation;",
"import org.tarantool.orm.internals.operations.UpdateOperation;",
"import org.tarantool.orm.internals.operations.UpsertOperation;",
"import test.DataClass;",

        "public final class DataClassManager {",
            "private final String spaceName = \"test\";",

            "private final TarantoolClient tarantoolClient;",

            "private final Meta<DataClass> meta;",

            "public DataClassManager(TarantoolClient tarantoolClient) {",
                "this.tarantoolClient = tarantoolClient;",
                "this.meta = new DataClassManagerMeta();",
            "}",

            "public SelectOperation<DataClass> selectUsingPrimaryIndex(final int id) {",
                "List<?> keys = Arrays.asList(id);",
                "return new SelectOperation<>(tarantoolClient, meta, spaceName, \"primary\", keys);",
            "}",

            "public InsertOperation<DataClass> insert(final DataClass value) {",
                "return new InsertOperation<>(tarantoolClient, meta, spaceName, value);",
            "}",

            "public DeleteOperation<DataClass> delete(final DataClass value) {",
                "List<Object> keys = new ArrayList<>();",
                "keys.add(value.getId());",
                "return new DeleteOperation<>(tarantoolClient, meta, spaceName, keys);",
            "}",

            "public ReplaceOperation<DataClass> replace(final DataClass value) {",
                "return new ReplaceOperation<>(tarantoolClient, meta, spaceName, value);",
            "}",

            "public UpdateOperation<DataClass> update(final DataClass value) {",
                "List<Object> keys = new ArrayList<>();",
                "keys.add(value.getId());",
                "List<List<?>> ops = new ArrayList<>();",
                "ops.add(Arrays.asList(\"=\", 1, value.getValue()));",
                "ops.add(Arrays.asList(\"=\", 2, value.getObjects()));",
                "ops.add(Arrays.asList(\"=\", 3, value.getLongs()));",
                "return new UpdateOperation<>(tarantoolClient, meta, spaceName, keys, ops);",
            "}",

            "public UpsertOperation<DataClass> upsert(final DataClass defaultValue,",
                                                     "final DataClass updatedValue) {",
                "List<Object> keys = new ArrayList<>();",
                "keys.add(defaultValue.getId());",
                "List<List<?>> ops = new ArrayList<>();",
                "ops.add(Arrays.asList(\"=\", 1, updatedValue.getValue()));",
                "ops.add(Arrays.asList(\"=\", 2, updatedValue.getObjects()));",
                "ops.add(Arrays.asList(\"=\", 3, updatedValue.getLongs()));",
                "return new UpsertOperation<>(tarantoolClient, meta, spaceName, keys, defaultValue, ops);",
            "}",

            "private final class DataClassManagerMeta extends Meta<DataClass> {",
                "public List<?> toList(final DataClass value) {",
                    "List<Object> result = new ArrayList<>();",
                    "result.add(value.getId());",
                    "result.add(value.getValue());",
                    "result.add(value.getObjects());",
                    "result.add(value.getLongs());",
                    "return result;",
                "}",

                "public DataClass fromList(final List<?> values) {",
                    "DataClass result = new DataClass();",
                    "result.setId(((Number) values.get(0)).intValue());",
                    "result.setValue(((Number) values.get(1)).intValue());",
                    "result.setObjects(((List<?>) values.get(2)).toArray(new Object[] {}));",
                    "result.setLongs(Longs.toArray((Collection<? extends Number>) values.get(3)));",
                    "return result;",
                "}",
            "}",
        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(managerOutput, managerFactoryOutput);
    }

    @Test
    public void tupleWithMultipleIndexes() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.IndexedField;",
                        "import org.tarantool.orm.annotations.IndexedFieldParams;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
                        "@Tuple(spaceName = \"test\", indexes = {@Index(name = \"primary\", isPrimary = true), @Index(name = \"secondary\")})",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private int id;",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"secondary\"))",
                        "private String value;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "public String getValue() {return value;}",
                        "public void setValue(String value) {this.value = value;}",
                        "}"
                )
        );

        final JavaFileObject managerOutput = JavaFileObjects.forSourceString(
                "org.tarantool.orm.generated.DataClassManager",
                Joiner.on(NEW_LINE).join(

                        "package org.tarantool.orm.generated;",

                    "import java.lang.Number;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "import java.util.ArrayList;",
                    "import java.util.Arrays;",
                    "import java.util.List;",
                    "import org.tarantool.TarantoolClient;",
                    "import org.tarantool.orm.internals.Meta;",
                    "import org.tarantool.orm.internals.operations.DeleteOperation;",
                    "import org.tarantool.orm.internals.operations.InsertOperation;",
                    "import org.tarantool.orm.internals.operations.ReplaceOperation;",
                    "import org.tarantool.orm.internals.operations.SelectOperation;",
                    "import org.tarantool.orm.internals.operations.UpdateOperation;",
                    "import org.tarantool.orm.internals.operations.UpsertOperation;",
                    "import test.DataClass;",

        "public final class DataClassManager {",
            "private final String spaceName = \"test\";",

            "private final TarantoolClient tarantoolClient;",

            "private final Meta<DataClass> meta;",

            "public DataClassManager(TarantoolClient tarantoolClient) {",
                "this.tarantoolClient = tarantoolClient;",
                "this.meta = new DataClassManagerMeta();",
            "}",

            "public SelectOperation<DataClass> selectUsingSecondaryIndex(final String value) {",
                "List<?> keys = Arrays.asList(value);",
                "return new SelectOperation<>(tarantoolClient, meta, spaceName, \"secondary\", keys);",
            "}",

            "public SelectOperation<DataClass> selectUsingPrimaryIndex(final int id) {",
                "List<?> keys = Arrays.asList(id);",
                "return new SelectOperation<>(tarantoolClient, meta, spaceName, \"primary\", keys);",
            "}",

            "public InsertOperation<DataClass> insert(final DataClass value) {",
                "return new InsertOperation<>(tarantoolClient, meta, spaceName, value);",
            "}",

            "public DeleteOperation<DataClass> delete(final DataClass value) {",
                "List<Object> keys = new ArrayList<>();",
                "keys.add(value.getId());",
                "return new DeleteOperation<>(tarantoolClient, meta, spaceName, keys);",
            "}",

            "public ReplaceOperation<DataClass> replace(final DataClass value) {",
                "return new ReplaceOperation<>(tarantoolClient, meta, spaceName, value);",
            "}",

            "public UpdateOperation<DataClass> update(final DataClass value) {",
                "List<Object> keys = new ArrayList<>();",
                "keys.add(value.getId());",
                "List<List<?>> ops = new ArrayList<>();",
                "ops.add(Arrays.asList(\"=\", 1, value.getValue()));",
                "return new UpdateOperation<>(tarantoolClient, meta, spaceName, keys, ops);",
            "}",

            "public UpsertOperation<DataClass> upsert(final DataClass defaultValue,",
                                                     "final DataClass updatedValue) {",
                "List<Object> keys = new ArrayList<>();",
                "keys.add(defaultValue.getId());",
                "List<List<?>> ops = new ArrayList<>();",
                "ops.add(Arrays.asList(\"=\", 1, updatedValue.getValue()));",
                "return new UpsertOperation<>(tarantoolClient, meta, spaceName, keys, defaultValue, ops);",
            "}",

            "private final class DataClassManagerMeta extends Meta<DataClass> {",
                "public List<?> toList(final DataClass value) {",
                    "List<Object> result = new ArrayList<>();",
                    "result.add(value.getId());",
                    "result.add(value.getValue());",
                    "return result;",
                "}",

                "public DataClass fromList(final List<?> values) {",
                    "DataClass result = new DataClass();",
                    "result.setId(((Number) values.get(0)).intValue());",
                    "result.setValue((String) values.get(1));",
                    "return result;",
                "}",
            "}",
        "}")
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(managerOutput, managerFactoryOutput);
    }

    @Test
    public void tupleWithCustomFieldOrder() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Field;",
                        "import org.tarantool.orm.annotations.IndexedField;",
                        "import org.tarantool.orm.annotations.IndexedFieldParams;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
                        "@Tuple(spaceName = \"test\", indexes = @Index(name = \"primary\", isPrimary = true))",
                        "public class DataClass {",
                        "@Field(position = 2)",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\", part = 2))",
                        "private int id;",
                        "@Field(position = 1)",
                        "private String value;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "public String getValue() {return value;}",
                        "public void setValue(String value) {this.value = value;}",
                        "}"
                )
        );

        final JavaFileObject managerOutput = JavaFileObjects.forSourceString(
                "org.tarantool.orm.generated.DataClassManager",
                Joiner.on(NEW_LINE).join(

                        "package org.tarantool.orm.generated;",

                    "import java.lang.Number;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "import java.util.ArrayList;",
                    "import java.util.Arrays;",
                    "import java.util.List;",
                    "import org.tarantool.TarantoolClient;",
                    "import org.tarantool.orm.internals.Meta;",
                    "import org.tarantool.orm.internals.operations.DeleteOperation;",
                    "import org.tarantool.orm.internals.operations.InsertOperation;",
                    "import org.tarantool.orm.internals.operations.ReplaceOperation;",
                    "import org.tarantool.orm.internals.operations.SelectOperation;",
                    "import org.tarantool.orm.internals.operations.UpdateOperation;",
                    "import org.tarantool.orm.internals.operations.UpsertOperation;",
                    "import test.DataClass;",

        "public final class DataClassManager {",
            "private final String spaceName = \"test\";",

            "private final TarantoolClient tarantoolClient;",

            "private final Meta<DataClass> meta;",

            "public DataClassManager(TarantoolClient tarantoolClient) {",
                "this.tarantoolClient = tarantoolClient;",
                "this.meta = new DataClassManagerMeta();",
            "}",

            "public SelectOperation<DataClass> selectUsingPrimaryIndex(final int id) {",
                "List<?> keys = Arrays.asList(id);",
                "return new SelectOperation<>(tarantoolClient, meta, spaceName, \"primary\", keys);",
            "}",

            "public InsertOperation<DataClass> insert(final DataClass value) {",
                "return new InsertOperation<>(tarantoolClient, meta, spaceName, value);",
            "}",

            "public DeleteOperation<DataClass> delete(final DataClass value) {",
                "List<Object> keys = new ArrayList<>();",
                "keys.add(value.getId());",
                "return new DeleteOperation<>(tarantoolClient, meta, spaceName, keys);",
            "}",

            "public ReplaceOperation<DataClass> replace(final DataClass value) {",
                "return new ReplaceOperation<>(tarantoolClient, meta, spaceName, value);",
            "}",

            "public UpdateOperation<DataClass> update(final DataClass value) {",
                "List<Object> keys = new ArrayList<>();",
                "keys.add(value.getId());",
                "List<List<?>> ops = new ArrayList<>();",
                "ops.add(Arrays.asList(\"=\", 0, value.getValue()));",
                "return new UpdateOperation<>(tarantoolClient, meta, spaceName, keys, ops);",
            "}",

            "public UpsertOperation<DataClass> upsert(final DataClass defaultValue,",
                                                     "final DataClass updatedValue) {",
                "List<Object> keys = new ArrayList<>();",
                "keys.add(defaultValue.getId());",
                "List<List<?>> ops = new ArrayList<>();",
                "ops.add(Arrays.asList(\"=\", 0, updatedValue.getValue()));",
                "return new UpsertOperation<>(tarantoolClient, meta, spaceName, keys, defaultValue, ops);",
            "}",

            "private final class DataClassManagerMeta extends Meta<DataClass> {",
                "public List<?> toList(final DataClass value) {",
                    "List<Object> result = new ArrayList<>();",
                    "result.add(value.getValue());",
                    "result.add(value.getId());",
                    "return result;",
                "}",

                "public DataClass fromList(final List<?> values) {",
                    "DataClass result = new DataClass();",
                    "result.setValue((String) values.get(0));",
                    "result.setId(((Number) values.get(1)).intValue());",
                    "return result;",
                "}",
            "}",
        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(managerOutput, managerFactoryOutput);
    }

    @Test
    public void tupleWithMultipleIndexesAndCustomIndexPart() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.IndexedField;",
                        "import org.tarantool.orm.annotations.IndexedFieldParams;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
                        "@Tuple(spaceName = \"test\", indexes = {@Index(name = \"primary\", isPrimary = true), @Index(name = \"secondary\")})",
                        "public class DataClass {",
                        "@IndexedField(indexes = {@IndexedFieldParams(indexName = \"primary\", part = 2), @IndexedFieldParams(indexName = \"secondary\", part = 1)})",
                        "private int id;",
                        "@IndexedField(indexes = {@IndexedFieldParams(indexName = \"primary\", part = 1), @IndexedFieldParams(indexName = \"secondary\", part = 2)})",
                        "private String value;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "public String getValue() {return value;}",
                        "public void setValue(String value) {this.value = value;}",
                        "}"
                )
        );

        final JavaFileObject managerOutput = JavaFileObjects.forSourceString(
                "org.tarantool.orm.generated.DataClassManager",
                Joiner.on(NEW_LINE).join(
                        "package org.tarantool.orm.generated;",

                        "import java.lang.Number;",
                        "import java.lang.Object;",
                        "import java.lang.String;",
                        "import java.util.ArrayList;",
                        "import java.util.Arrays;",
                        "import java.util.List;",
                        "import org.tarantool.TarantoolClient;",
                        "import org.tarantool.orm.internals.Meta;",
                        "import org.tarantool.orm.internals.operations.DeleteOperation;",
                        "import org.tarantool.orm.internals.operations.InsertOperation;",
                        "import org.tarantool.orm.internals.operations.ReplaceOperation;",
                        "import org.tarantool.orm.internals.operations.SelectOperation;",
                        "import org.tarantool.orm.internals.operations.UpdateOperation;",
                        "import org.tarantool.orm.internals.operations.UpsertOperation;",
                        "import test.DataClass;",

        "public final class DataClassManager {",
            "private final String spaceName = \"test\";",

            "private final TarantoolClient tarantoolClient;",

            "private final Meta<DataClass> meta;",

            "public DataClassManager(TarantoolClient tarantoolClient) {",
                "this.tarantoolClient = tarantoolClient;",
                "this.meta = new DataClassManagerMeta();",
            "}",

            "public SelectOperation<DataClass> selectUsingSecondaryIndex(final int id, final String value) {",
                "List<?> keys = Arrays.asList(id, value);",
                "return new SelectOperation<>(tarantoolClient, meta, spaceName, \"secondary\", keys);",
            "}",

            "public SelectOperation<DataClass> selectUsingPrimaryIndex(final String value, final int id) {",
                "List<?> keys = Arrays.asList(value, id);",
                "return new SelectOperation<>(tarantoolClient, meta, spaceName, \"primary\", keys);",
            "}",

            "public InsertOperation<DataClass> insert(final DataClass value) {",
                "return new InsertOperation<>(tarantoolClient, meta, spaceName, value);",
            "}",

            "public DeleteOperation<DataClass> delete(final DataClass value) {",
                "List<Object> keys = new ArrayList<>();",
                "keys.add(value.getValue());",
                "keys.add(value.getId());",
                "return new DeleteOperation<>(tarantoolClient, meta, spaceName, keys);",
            "}",

            "public ReplaceOperation<DataClass> replace(final DataClass value) {",
                "return new ReplaceOperation<>(tarantoolClient, meta, spaceName, value);",
            "}",

            "public UpdateOperation<DataClass> update(final DataClass value) {",
                "List<Object> keys = new ArrayList<>();",
                "keys.add(value.getValue());",
                "keys.add(value.getId());",
                "List<List<?>> ops = new ArrayList<>();",
                "return new UpdateOperation<>(tarantoolClient, meta, spaceName, keys, ops);",
            "}",

            "public UpsertOperation<DataClass> upsert(final DataClass defaultValue,",
                                                     "final DataClass updatedValue) {",
                "List<Object> keys = new ArrayList<>();",
                "keys.add(defaultValue.getValue());",
                "keys.add(defaultValue.getId());",
                "List<List<?>> ops = new ArrayList<>();",
                "return new UpsertOperation<>(tarantoolClient, meta, spaceName, keys, defaultValue, ops);",
            "}",

            "private final class DataClassManagerMeta extends Meta<DataClass> {",
                "public List<?> toList(final DataClass value) {",
                    "List<Object> result = new ArrayList<>();",
                    "result.add(value.getId());",
                    "result.add(value.getValue());",
                    "return result;",
                "}",

                "public DataClass fromList(final List<?> values) {",
                    "DataClass result = new DataClass();",
                     "result.setId(((Number) values.get(0)).intValue());",
                    "result.setValue((String) values.get(1));",
                    "return result;",
                "}",
            "}",
        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(managerOutput, managerFactoryOutput);
    }

    @Test
    public void byteField() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private byte id;",
                        "public byte getId() {return id;}",
                        "public void setId(byte id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError();
    }

    @Test
    public void shortField() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private short id;",
                        "public short getId() {return id;}",
                        "public void setId(short id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError();
    }

    @Test
    public void intField() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private int id;",
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError();
    }

    @Test
    public void floatField() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private float id;",
                        "public float getId() {return id;}",
                        "public void setId(float id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError();
    }

    @Test
    public void longField() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private long id;",
                        "public long getId() {return id;}",
                        "public void setId(long id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError();
    }

    @Test
    public void doubleField() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private double id;",
                        "public double getId() {return id;}",
                        "public void setId(double id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError();
    }

    @Test
    public void characterField() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private char id;",
                        "public char getId() {return id;}",
                        "public void setId(char id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError();
    }

    @Test
    public void arrayOfIntField() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private int[] id;",
                        "public int[] getId() {return id;}",
                        "public void setId(int[] id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError();
    }

    @Test
    public void arrayOfObjectsField() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private Object[] id;",
                        "public Object[] getId() {return id;}",
                        "public void setId(Object[] id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError();
    }

    @Test
    public void mapField() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.*;",
                        "import java.util.Map;",
                        "@Tuple(indexes = @Index(name = \"primary\", isPrimary = true), spaceName = \"test\")",
                        "public class DataClass {",
                        "@IndexedField(indexes = @IndexedFieldParams(indexName = \"primary\"))",
                        "private Map<String, Object> id;",
                        "public Map<String, Object> getId() {return id;}",
                        "public void setId(Map<String, Object> id) {this.id = id;}",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new TupleManagerProcessor())
                .compilesWithoutError();
    }
}
