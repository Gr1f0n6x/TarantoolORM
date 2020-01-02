package org.tarantool.auto;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.tarantool.TarantoolClient;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;

final class TupleManagerGenerator {
    public TupleManagerGenerator() {
    }

    public void generate(Filer filer, TupleMeta tupleMeta) throws IOException {
        TypeSpec newClass = TypeSpec.classBuilder(tupleMeta.className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(spaceName(tupleMeta.spaceName))
                .addField(TarantoolClient.class, "tarantoolClient", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(createConstructor())
                .build();

        JavaFile javaFile = JavaFile.builder(Common.PACKAGE_NAME, newClass)
                .build();

        javaFile.writeTo(filer);
    }

    private FieldSpec spaceName(String spaceName) {
        return FieldSpec
                .builder(String.class, "spaceName", Modifier.PRIVATE, Modifier.FINAL)
                .initializer("$S", spaceName)
                .build();
    }

    private MethodSpec createConstructor() {
        MethodSpec constructor = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TarantoolClient.class, "tarantoolClient")
                .addStatement("this.$N = $N", "tarantoolClient", "tarantoolClient")
                .build();

        return constructor;
    }
}
