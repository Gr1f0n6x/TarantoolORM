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
    public void simpleTuple() {
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
                        "}"
                )
        );

        final JavaFileObject managerOutput = JavaFileObjects.forSourceString(
                "org.tarantool.orm.generated.DataClassManager",
                Joiner.on(NEW_LINE).join(
                        "package org.tarantool.orm.generated;",
                        "",
                        "",
                        "import java.lang.String;",
                        "import org.tarantool.TarantoolClient",
                        "public final class DataClassManager {",
                        "private final String spaceName = \"test\";",
                        "private final TarantoolClient tarantoolClient;",
                        "",
                        "public DataClassManager(TarantoolClient tarantoolClient) {",
                        "this.tarantoolClient = tarantoolClient;",
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
