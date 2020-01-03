package org.tarantool.auto;

import com.squareup.javapoet.*;
import org.tarantool.Iterator;
import org.tarantool.TarantoolClient;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

final class TupleManagerGenerator {
    private final ClassName list = ClassName.get("java.util", "List");
    private final ParameterizedTypeName wildCardList = ParameterizedTypeName.get(list, WildcardTypeName.subtypeOf(Object.class));
    private final ParameterizedTypeName listOfObjects = ParameterizedTypeName.get(list, ClassName.OBJECT);
    private final ClassName arrayList = ClassName.get("java.util", "ArrayList");

    public TupleManagerGenerator() {
    }

    public void generate(Filer filer, Types typeUtil, TupleMeta tupleMeta) throws IOException {
        TypeSpec newClass = TypeSpec.classBuilder(tupleMeta.className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(spaceName(tupleMeta.spaceName))
                .addField(TarantoolClient.class, "tarantoolClient", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(generateConstructor())
                .addMethod(generateDataClassToListMethod(tupleMeta))
                .addMethod(generateListToDataClassMethod(tupleMeta, typeUtil))
                .addMethods(generateSelectMethods(tupleMeta))
                .addMethod(generateInsertMethod(tupleMeta))
                .addMethod(generateDeleteMethod(tupleMeta))
                .addMethod(generateReplaceMethod(tupleMeta))
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

    private MethodSpec generateConstructor() {
        return MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TarantoolClient.class, "tarantoolClient")
                .addStatement("this.$N = $N", "tarantoolClient", "tarantoolClient")
                .build();
    }

    private MethodSpec generateListToDataClassMethod(TupleMeta tupleMeta, Types typeUtil) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("fromList")
                .addModifiers(Modifier.PRIVATE)
                .returns(tupleMeta.classType);

        ClassName list = ClassName.get("java.util", "List");
        ParameterizedTypeName wildCardList = ParameterizedTypeName.get(list, WildcardTypeName.subtypeOf(Object.class));

        builder.addParameter(wildCardList, "values", Modifier.FINAL);

        List<FieldMeta> sortedFieldList = tupleMeta.fields
                .stream()
                .sorted(Comparator.comparingInt(meta -> meta.position))
                .collect(Collectors.toList());

        builder.addStatement("$T result = new $T()", tupleMeta.classType, tupleMeta.classType);

        int index = 0;

        for (FieldMeta fieldMeta : sortedFieldList) {
            TypeMirror valueType = fieldMeta.field.asType();
            if (fieldMeta.field.asType().getKind().isPrimitive()) {
                valueType = typeUtil.boxedClass((PrimitiveType) fieldMeta.field.asType()).asType();
            }

            builder.addStatement("result.$L(($T) values.get($L))", fieldMeta.setterName, valueType, index);
            index++;
        }

        builder.addStatement("return result");

        return builder.build();
    }

    private MethodSpec generateDataClassToListMethod(TupleMeta tupleMeta) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("toList")
                .addModifiers(Modifier.PRIVATE)
                .returns(wildCardList);

        builder.addParameter(tupleMeta.classType, "value", Modifier.FINAL);

        List<FieldMeta> sortedFieldList = tupleMeta.fields
                .stream()
                .sorted(Comparator.comparingInt(meta -> meta.position))
                .collect(Collectors.toList());

        builder.addStatement("$T result = new $T<>()", listOfObjects, arrayList);

        for (FieldMeta fieldMeta : sortedFieldList) {
            builder.addStatement("result.add(value.$L())", fieldMeta.getterName);
        }

        builder.addStatement("return result");

        return builder.build();
    }

    private MethodSpec generateInsertMethod(TupleMeta tupleMeta) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("insertSync")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(tupleMeta.classType, "value", Modifier.FINAL)
                .returns(tupleMeta.classType);

        builder.addStatement("$T values = toList(value)", wildCardList);
        builder.addStatement("$T result = this.$N.syncOps().insert($S, values)", wildCardList, "tarantoolClient", tupleMeta.spaceName);
        builder.beginControlFlow("if (result.size() == 1)");
        builder.addStatement("return fromList(($T) result.get(0))", wildCardList);
        builder.nextControlFlow("else");
        builder.addStatement("return null");
        builder.endControlFlow();

        return builder.build();
    }

    private MethodSpec generateReplaceMethod(TupleMeta tupleMeta) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("replaceSync")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(tupleMeta.classType, "value", Modifier.FINAL)
                .returns(tupleMeta.classType);

        builder.addStatement("$T values = toList(value)", wildCardList);
        builder.addStatement("$T result = this.$N.syncOps().replace($S, values)", wildCardList, "tarantoolClient", tupleMeta.spaceName);
        builder.beginControlFlow("if (result.size() == 1)");
        builder.addStatement("return fromList(($T) result.get(0))", wildCardList);
        builder.nextControlFlow("else");
        builder.addStatement("return null");
        builder.endControlFlow();

        return builder.build();
    }

    private MethodSpec generateDeleteMethod(TupleMeta tupleMeta) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("deleteSync")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(tupleMeta.classType, "value", Modifier.FINAL)
                .returns(tupleMeta.classType);

        List<IndexFieldMeta> indexFieldMetas = tupleMeta.indexedFields.get(tupleMeta.primaryIndexName);

        builder.addStatement("$T keys = new $T<>()", listOfObjects, arrayList);

        for (IndexFieldMeta indexFieldMeta : indexFieldMetas) {
            builder.addStatement("keys.add(value.$L())", indexFieldMeta.getterName);
        }

        builder.addStatement("$T result = this.$N.syncOps().delete($S, keys)", wildCardList, "tarantoolClient", tupleMeta.spaceName);
        builder.beginControlFlow("if (result.size() == 1)");
        builder.addStatement("return fromList(($T) result.get(0))", wildCardList);
        builder.nextControlFlow("else");
        builder.addStatement("return null");
        builder.endControlFlow();

        return builder.build();
    }

    private Iterable<MethodSpec> generateSelectMethods(TupleMeta tupleMeta) {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        tupleMeta.indexedFields.forEach((indexName, fields) -> {
            fields.sort(Comparator.comparingInt(o -> o.part));

            MethodSpec.Builder builder = MethodSpec.methodBuilder("selectUsing" + Common.capitalize(indexName) + "IndexSync");
            builder.returns(tupleMeta.classType);
            builder.addModifiers(Modifier.PUBLIC);
            builder.addParameters(getParametersForSelect(fields, tupleMeta.indexMetas.get(indexName)));
            builder.addCode(getSelectStatement(fields, tupleMeta, tupleMeta.indexMetas.get(indexName)));

            methodSpecs.add(builder.build());
        });

        return methodSpecs;
    }

    private Iterable<ParameterSpec> getParametersForSelect(List<IndexFieldMeta> indexFieldMetas, IndexMeta indexMeta) {
        return indexFieldMetas.stream().map(meta -> {
            ParameterSpec.Builder builder = ParameterSpec
                    .builder(meta.fieldType, meta.indexField.getSimpleName().toString(), Modifier.FINAL);

            if (meta.isNullable && indexMeta.isPrimary) {
                throw new IllegalArgumentException("Primary index can't have nullable fields");
            }

            return builder.build();
        }).collect(Collectors.toList());
    }

    private CodeBlock getSelectStatement(List<IndexFieldMeta> indexFieldMetas, TupleMeta tupleMeta, IndexMeta indexMeta) {
        CodeBlock.Builder builder = CodeBlock.builder();

        String arguments = indexFieldMetas.stream().map(meta -> meta.indexField.getSimpleName().toString()).collect(Collectors.joining(", "));
        builder.addStatement("$T keys = $T.asList($L)", wildCardList, Arrays.class, arguments);
        builder.addStatement("$T result = this.$N.syncOps().select($S, $S, keys, $L, $L, $T.EQ)", wildCardList, "tarantoolClient", tupleMeta.spaceName, indexMeta.name, 0, 1, Iterator.class);
        builder.beginControlFlow("if (result.size() == 1)");
        builder.addStatement("return fromList(($T) result.get(0))", wildCardList);
        builder.nextControlFlow("else");
        builder.addStatement("return null");
        builder.endControlFlow();

        return builder.build();
    }
}
