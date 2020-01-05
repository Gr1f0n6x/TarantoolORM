package org.tarantool.auto;

import com.squareup.javapoet.*;
import org.tarantool.TarantoolClient;
import org.tarantool.internals.Meta;
import org.tarantool.internals.operations.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
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

    public void generate(Filer filer, TupleMeta tupleMeta) throws IOException {
        TypeSpec newClass = TypeSpec.classBuilder(tupleMeta.className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(spaceName(tupleMeta.spaceName))
                .addField(TarantoolClient.class, "tarantoolClient", Modifier.PRIVATE, Modifier.FINAL)
                .addField(ParameterizedTypeName.get(ClassName.get(Meta.class), tupleMeta.classType), "meta", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(generateConstructor(tupleMeta))
                .addType(dataClassMeta(tupleMeta))
                .addMethods(generateSelectMethods(tupleMeta))
                .addMethod(generateInsertMethod(tupleMeta))
                .addMethod(generateDeleteMethod(tupleMeta))
                .addMethod(generateReplaceMethod(tupleMeta))
                .addMethod(generateUpdateMethod(tupleMeta))
                .addMethod(generateUpsertMethod(tupleMeta))
                .build();

        JavaFile javaFile = JavaFile.builder(Common.PACKAGE_NAME, newClass)
                .build();

        javaFile.writeTo(filer);
    }

    private TypeSpec dataClassMeta(TupleMeta tupleMeta) {
        return TypeSpec.classBuilder(tupleMeta.className + "Meta")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(Meta.class), tupleMeta.classType))
                .addMethod(generateDataClassToListMethod(tupleMeta))
                .addMethod(generateListToDataClassMethod(tupleMeta))
                .build();
    }

    private FieldSpec spaceName(String spaceName) {
        return FieldSpec
                .builder(String.class, "spaceName", Modifier.PRIVATE, Modifier.FINAL)
                .initializer("$S", spaceName)
                .build();
    }

    private MethodSpec generateConstructor(TupleMeta tupleMeta) {
        return MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TarantoolClient.class, "tarantoolClient")
                .addStatement("this.$N = $N", "tarantoolClient", "tarantoolClient")
                // fixme: use type ($T)
                .addStatement("this.$N = new $L()", "meta", tupleMeta.className + "Meta")
                .build();
    }

    private MethodSpec generateListToDataClassMethod(TupleMeta tupleMeta) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("fromList")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(wildCardList, "values", Modifier.FINAL)
                .returns(tupleMeta.classType)
                .addStatement("$T result = new $T()", tupleMeta.classType, tupleMeta.classType);

        for (FieldMeta fieldMeta : tupleMeta.fields) {
            builder.addStatement("result.$L(($T) values.get($L))", fieldMeta.setterName, fieldMeta.valueType, fieldMeta.getIndex());
        }

        builder.addStatement("return result");

        return builder.build();
    }

    private MethodSpec generateDataClassToListMethod(TupleMeta tupleMeta) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("toList")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(tupleMeta.classType, "value", Modifier.FINAL)
                .returns(wildCardList)
                .addStatement("$T result = new $T<>()", listOfObjects, arrayList);

        for (FieldMeta fieldMeta : tupleMeta.fields) {
            builder.addStatement("result.add(value.$L())", fieldMeta.getterName);
        }

        builder.addStatement("return result");

        return builder.build();
    }

    private MethodSpec generateUpdateMethod(TupleMeta tupleMeta) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(tupleMeta.classType, "value", Modifier.FINAL)
                .returns(ParameterizedTypeName.get(ClassName.get(UpdateOperation.class), tupleMeta.classType));

        List<IndexFieldMeta> indexFieldMetas = tupleMeta.indexedFields.get(tupleMeta.primaryIndexName);

        builder.addStatement("$T keys = new $T<>()", listOfObjects, arrayList);

        for (IndexFieldMeta indexFieldMeta : indexFieldMetas) {
            builder.addStatement("keys.add(value.$L())", indexFieldMeta.getterName);
        }

        builder.addStatement("$T<$T> ops = new $T<>()", list, wildCardList, arrayList);
        // filter fields which are used as primary index because we can't update them
        tupleMeta.fields
                .stream()
                .filter(fieldMeta -> fieldMeta.indexFieldMetas
                        .stream()
                        .noneMatch(indexFieldMeta -> indexFieldMeta.indexName.equals(tupleMeta.primaryIndexName)))
                .forEach(fieldMeta ->
                        builder.addStatement("ops.add($T.asList($S, $L, value.$L()))", Arrays.class, "=", fieldMeta.getRealPosition(), fieldMeta.getterName)
                );

        builder.addStatement("return new $T<>(tarantoolClient, meta, spaceName, keys, ops)", UpdateOperation.class);

        return builder.build();
    }

    private MethodSpec generateUpsertMethod(TupleMeta tupleMeta) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("upsert")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(tupleMeta.classType, "defaultValue", Modifier.FINAL)
                .addParameter(tupleMeta.classType, "updatedValue", Modifier.FINAL)
                .returns(ParameterizedTypeName.get(ClassName.get(UpsertOperation.class), tupleMeta.classType));

        List<IndexFieldMeta> indexFieldMetas = tupleMeta.indexedFields.get(tupleMeta.primaryIndexName);

        builder.addStatement("$T keys = new $T<>()", listOfObjects, arrayList);

        for (IndexFieldMeta indexFieldMeta : indexFieldMetas) {
            builder.addStatement("keys.add(defaultValue.$L())", indexFieldMeta.getterName);
        }

        builder.addStatement("$T<$T> ops = new $T<>()", list, wildCardList, arrayList);
        // filter fields which are used as primary index because we can't update them
        tupleMeta.fields
                .stream()
                .filter(fieldMeta -> fieldMeta.indexFieldMetas
                        .stream()
                        .noneMatch(indexFieldMeta -> indexFieldMeta.indexName.equals(tupleMeta.primaryIndexName)))
                .forEach(fieldMeta ->
                        builder.addStatement("ops.add($T.asList($S, $L, updatedValue.$L()))", Arrays.class, "=", fieldMeta.getRealPosition(), fieldMeta.getterName)
                );

        builder.addStatement("return new $T<>(tarantoolClient, meta, spaceName, keys, defaultValue, ops)", UpsertOperation.class);

        return builder.build();
    }

    private MethodSpec generateInsertMethod(TupleMeta tupleMeta) {
        return MethodSpec.methodBuilder("insert")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(tupleMeta.classType, "value", Modifier.FINAL)
                .returns(ParameterizedTypeName.get(ClassName.get(InsertOperation.class), tupleMeta.classType))
                .addStatement("return new $T<>(tarantoolClient, meta, spaceName, value)", InsertOperation.class)
                .build();
    }

    private MethodSpec generateReplaceMethod(TupleMeta tupleMeta) {
        return MethodSpec.methodBuilder("replace")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(tupleMeta.classType, "value", Modifier.FINAL)
                .returns(ParameterizedTypeName.get(ClassName.get(ReplaceOperation.class), tupleMeta.classType))
                .addStatement("return new $T<>(tarantoolClient, meta, spaceName, value)", ReplaceOperation.class)
                .build();
    }

    private MethodSpec generateDeleteMethod(TupleMeta tupleMeta) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(tupleMeta.classType, "value", Modifier.FINAL)
                .returns(ParameterizedTypeName.get(ClassName.get(DeleteOperation.class), tupleMeta.classType));

        List<IndexFieldMeta> indexFieldMetas = tupleMeta.indexedFields.get(tupleMeta.primaryIndexName);

        builder.addStatement("$T keys = new $T<>()", listOfObjects, arrayList);

        for (IndexFieldMeta indexFieldMeta : indexFieldMetas) {
            builder.addStatement("keys.add(value.$L())", indexFieldMeta.getterName);
        }

        builder.addStatement("return new $T<>(tarantoolClient, meta, spaceName, keys)", DeleteOperation.class);

        return builder.build();
    }

    private Iterable<MethodSpec> generateSelectMethods(TupleMeta tupleMeta) {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        tupleMeta.indexedFields.forEach((indexName, fields) -> {
            fields.sort(Comparator.comparingInt(o -> o.part));

            MethodSpec.Builder builder = MethodSpec.methodBuilder("selectUsing" + Common.capitalize(indexName) + "Index");
            builder.returns(ParameterizedTypeName.get(ClassName.get(SelectOperation.class), tupleMeta.classType));
            builder.addModifiers(Modifier.PUBLIC);
            builder.addParameters(getParametersForSelect(fields, tupleMeta.indexMetas.get(indexName)));
            builder.addCode(getSelectStatement(fields, tupleMeta.indexMetas.get(indexName)));

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

    private CodeBlock getSelectStatement(List<IndexFieldMeta> indexFieldMetas, IndexMeta indexMeta) {
        CodeBlock.Builder builder = CodeBlock.builder();

        String arguments = indexFieldMetas.stream().map(meta -> meta.indexField.getSimpleName().toString()).collect(Collectors.joining(", "));
        builder.addStatement("$T keys = $T.asList($L)", wildCardList, Arrays.class, arguments);
        builder.addStatement("return new $T<>(tarantoolClient, meta, spaceName, $S, keys)", SelectOperation.class, indexMeta.name);

        return builder.build();
    }
}
