package org.tarantool.orm;

import com.google.common.base.Joiner;
import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import org.junit.Test;
import org.tarantool.auto.TupleManagerProcessor;

import javax.tools.JavaFileObject;

import java.util.Arrays;

import static com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Text.NEW_LINE;

public class TupleProcessorTest {
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
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
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
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
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
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
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
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
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
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
                        "@Tuple(indexes = @Index(name = \"\"), spaceName = \"test\")",
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
                .withErrorContaining("Index name should not be empty");
    }

    @Test
    public void noIndexError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
                        "@Tuple(indexes = {}, spaceName = \"test\")",
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
                .withErrorContaining("Tuple DataClass must have at least one index");
    }

    @Test
    public void duplicateIndexError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
                        "@Tuple(indexes = {@Index(name = \"primary\", isPrimary = true), @Index(name = \"primary\", isPrimary = true)}, spaceName = \"test\")",
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
                .withErrorContaining("Index names should be unique");
    }

    @Test
    public void noPrimaryIndexError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
                        "@Tuple(indexes = @Index(name = \"primary\"), spaceName = \"test\")",
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
                .withErrorContaining("Tuple DataClass does not have primary index");
    }

    @Test
    public void interfaceAnnotatedByTupleError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
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
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
                        "@Tuple(indexes = @Index(name = \"primary\"), spaceName = \"\")",
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
                .withErrorContaining("Space name should not be empty");
    }

    @Test
    public void dataClassWithoutFieldsError() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "test.DataClass",
                Joiner.on(NEW_LINE).join(
                        "package test;",
                        "",
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
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
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
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
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
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
                        "import org.tarantool.orm.annotations.Tuple;",
                        "import org.tarantool.orm.annotations.Index;",
                        "",
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
                        "public int getId() {return id;}",
                        "public void setId(int id) {this.id = id;}",
                        "}"
                )
        );

        final JavaFileObject managerOutput = JavaFileObjects.forSourceString(
                "org.tarantool.orm.generated.DataClassManager",
                Joiner.on(NEW_LINE).join(
                        "package org.tarantool.orm.generated;",
                        "",
                        "",
                        "import java.lang.Integer;",
                        "import java.lang.Object;",
                        "import java.lang.String;",
                        "import java.util.ArrayList;",
                        "import java.util.Arrays;",
                        "import java.util.List;",
                        "import org.tarantool.Iterator;",
                        "import org.tarantool.TarantoolClient;",
                        "import test.DataClass;",
                        "public final class DataClassManager {",
                        "private final String spaceName = \"test\";",
                        "private final TarantoolClient tarantoolClient;",
                        "",
                        "public DataClassManager(TarantoolClient tarantoolClient) {",
                        "this.tarantoolClient = tarantoolClient;",
                        "}",
                        "private List<?> toList(final DataClass value) {",
                        "List<Object> result = new ArrayList<>();",
                        "result.add(value.getId());",
                        "return result;",
                        "}",
                        "private DataClass fromList(final List<?> values) {",
                        "DataClass result = new DataClass();",
                        "result.setId((Integer) values.get(0));",
                        "return result;",
                        "}",
                        "public DataClass selectUsingPrimaryIndexSync(final int id) {",
                        "List<?> keys = Arrays.asList(id);",
                        "List<?> result = this.tarantoolClient.syncOps().select(\"test\", \"primary\", keys, 0, 1, Iterator.EQ);",
                        "if (result.size() == 1) {",
                        "return fromList((List<?>) result.get(0));",
                        " } else {",
                        "return null;",
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
}
