package org.tarantool.auto;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;

final class TupleManagerGenerator {
    public TupleManagerGenerator() {
    }

    public void generate(Filer filer, TupleMeta tupleMeta) throws IOException {
        TypeSpec newClass = TypeSpec.classBuilder(tupleMeta.className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .build();

        JavaFile javaFile = JavaFile.builder(Common.PACKAGE_NAME, newClass)
                .build();

        javaFile.writeTo(filer);
    }
}
