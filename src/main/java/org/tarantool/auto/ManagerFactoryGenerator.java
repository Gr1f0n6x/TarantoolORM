package org.tarantool.auto;

import com.squareup.javapoet.*;
import org.tarantool.TarantoolClient;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class ManagerFactoryGenerator {
    public ManagerFactoryGenerator() {
    }

    public void generate(Filer filer, List<TupleMeta> metas) throws IOException {
        TypeSpec managerFactory = TypeSpec.classBuilder("ManagerFactory")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(TarantoolClient.class, "tarantoolClient", Modifier.PRIVATE, Modifier.FINAL)
                .addMethods(methodSpecs(metas))
                .addMethod(createConstructor())
                .build();

        JavaFile javaFile = JavaFile.builder(Common.PACKAGE_NAME, managerFactory)
                .build();

        javaFile.writeTo(filer);
    }

    //todo: check duplicate names in different packages
    private Iterable<MethodSpec> methodSpecs(List<TupleMeta> metas) {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        for (TupleMeta meta : metas) {
            ClassName generatedClass = ClassName.get(Common.PACKAGE_NAME, meta.className);

            MethodSpec spec = MethodSpec
                    .methodBuilder(methodName(meta.initialClassName))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(generatedClass)
                    .addStatement("return new $T()", generatedClass)
                    .build();

            methodSpecs.add(spec);
        }

        return methodSpecs;
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

    private String methodName(String className) {
        return className.substring(0, 1).toLowerCase() + className.substring(1) + "Manager";
    }
}
