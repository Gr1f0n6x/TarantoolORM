package org.tarantool.auto;

import com.google.common.collect.Sets;
import com.squareup.javapoet.*;
import org.tarantool.TarantoolClient;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

final class TupleManagerGenerator {
    public TupleManagerGenerator() {
    }

    public void generate(Filer filer, TupleMeta tupleMeta) throws IOException {
        Map<String, List<IndexFieldMeta>> indexedFields = getIndexedFields(tupleMeta.fields);

        TypeSpec newClass = TypeSpec.classBuilder(tupleMeta.className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(spaceName(tupleMeta.spaceName))
                .addField(TarantoolClient.class, "tarantoolClient", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(createConstructor())
                .addMethod(getDataClassToListMethod(tupleMeta))
                .addMethod(getListToDataClassMethod(tupleMeta))
                .addMethods(generateSelectMethods(indexedFields, tupleMeta))
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
        return MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TarantoolClient.class, "tarantoolClient")
                .addStatement("this.$N = $N", "tarantoolClient", "tarantoolClient")
                .build();
    }

    private MethodSpec getListToDataClassMethod(TupleMeta tupleMeta) {
        TypeName dataClassType = TypeName.get(tupleMeta.classElement.asType());

        MethodSpec.Builder builder = MethodSpec.methodBuilder("fromList")
                .addModifiers(Modifier.PRIVATE)
                .returns(dataClassType);

        ClassName list = ClassName.get("java.util", "List");
        ParameterizedTypeName wildCardList = ParameterizedTypeName.get(list, WildcardTypeName.subtypeOf(Object.class));

        builder.addParameter(wildCardList, "values", Modifier.FINAL);

        List<FieldMeta> sortedFieldList = tupleMeta.fields
                .stream()
                .sorted(Comparator.comparingInt(meta -> meta.position))
                .collect(Collectors.toList());

        builder.addStatement("$T result = new $T()", dataClassType, dataClassType);

        int index = 0;

        for (FieldMeta fieldMeta : sortedFieldList) {
            builder.addStatement("result.$L(values.get(($T) $L))", fieldMeta.setterName, fieldMeta.tupleField, index);
            index++;
        }

        builder.addStatement("return result");

        return builder.build();
    }

    private MethodSpec getDataClassToListMethod(TupleMeta tupleMeta) {
        ClassName list = ClassName.get("java.util", "List");
        ParameterizedTypeName wildCardList = ParameterizedTypeName.get(list, WildcardTypeName.subtypeOf(Object.class));
        ClassName arrayList = ClassName.get("java.util", "ArrayList");
        TypeName dataClassType = TypeName.get(tupleMeta.classElement.asType());

        MethodSpec.Builder builder = MethodSpec.methodBuilder("toList")
                .addModifiers(Modifier.PRIVATE)
                .returns(wildCardList);

        builder.addParameter(dataClassType, "value", Modifier.FINAL);

        List<FieldMeta> sortedFieldList = tupleMeta.fields
                .stream()
                .sorted(Comparator.comparingInt(meta -> meta.position))
                .collect(Collectors.toList());

        builder.addStatement("$T result = new $T<>()", wildCardList, arrayList);

        for (FieldMeta fieldMeta : sortedFieldList) {
            builder.addStatement("result.add(value.$L())", fieldMeta.getterName, fieldMeta.tupleField);
        }

        builder.addStatement("return result");

        return builder.build();
    }

    private Map<String, List<IndexFieldMeta>> getIndexedFields(List<FieldMeta> fieldMetas) {
        List<FieldMeta> indexedFields = fieldMetas.stream().filter(field -> field.isIndexed).collect(Collectors.toList());

        if (indexedFields.isEmpty()) {
            throw new IllegalArgumentException("Data class must have at least one indexed field");
        }

        return indexedFields
                .stream()
                .flatMap(field -> field.indexFieldMetas.stream())
                .collect(Collectors.groupingBy(
                        indexFieldMeta -> indexFieldMeta.indexName,
                        Collectors.mapping(Function.identity(), Collectors.toList())
                ));
    }

    private Iterable<MethodSpec> generateSelectMethods(Map<String, List<IndexFieldMeta>> indexedFields, TupleMeta tupleMeta) {
        Sets.SetView<String> difference = Sets.symmetricDifference(
                tupleMeta.indexMetas.keySet(),
                indexedFields.keySet()
        );

        if (!difference.isEmpty()) {
            throw new IllegalArgumentException("Name of indexes in @IndexedField and in @Index must correspond to each other");
        }

        List<MethodSpec> methodSpecs = new ArrayList<>();

        indexedFields.forEach((indexName, fields) -> {
            fields.sort(Comparator.comparingInt(o -> o.part));

            MethodSpec.Builder builder = MethodSpec.methodBuilder("selectUsing" + Common.capitalize(indexName) + "IndexSync");
            builder.returns(TypeName.get(tupleMeta.classElement.asType()));
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
                    .builder(TypeName.get(meta.tupleField.asType()), meta.tupleField.getSimpleName().toString(), Modifier.FINAL);

            if (meta.isNullable && indexMeta.isPrimary) {
                throw new IllegalArgumentException("Primary index can't have nullable fields");
            }

            return builder.build();
        }).collect(Collectors.toList());
    }

    private CodeBlock getSelectStatement(List<IndexFieldMeta> indexFieldMetas, TupleMeta tupleMeta, IndexMeta indexMeta) {
        CodeBlock.Builder builder = CodeBlock.builder();

        ClassName list = ClassName.get("java.util", "List");
        ParameterizedTypeName wildCardList = ParameterizedTypeName.get(list, WildcardTypeName.subtypeOf(Object.class));

        String arguments = indexFieldMetas.stream().map(meta -> meta.tupleField.getSimpleName().toString()).collect(Collectors.joining(", "));
        builder.addStatement("$T keys = $T.asList($L)", wildCardList, Arrays.class, arguments);
        builder.addStatement("$T result = this.$N.syncOps().select($S, $S, keys, $L, $L)", wildCardList, "tarantoolClient", tupleMeta.spaceName, indexMeta.name, 0, 1);
        builder.addStatement("return fromList(result)");

        return builder.build();
    }
}
