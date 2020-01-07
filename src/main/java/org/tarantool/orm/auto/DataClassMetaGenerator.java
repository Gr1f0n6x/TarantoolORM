package org.tarantool.orm.auto;

import com.google.common.primitives.*;
import com.squareup.javapoet.*;
import org.tarantool.orm.internals.Meta;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

final class DataClassMetaGenerator {
    private final ClassName list = ClassName.get("java.util", "List");
    private final ParameterizedTypeName wildCardList = ParameterizedTypeName.get(list, WildcardTypeName.subtypeOf(Object.class));
    private final ParameterizedTypeName listOfObjects = ParameterizedTypeName.get(list, ClassName.OBJECT);
    private final ClassName arrayList = ClassName.get("java.util", "ArrayList");
    private final ClassName collection = ClassName.get("java.util", "Collection");
    private final ParameterizedTypeName subtypeOfNumber = ParameterizedTypeName.get(collection, WildcardTypeName.subtypeOf(Number.class));
    private final ParameterizedTypeName collectionOfBooleans = ParameterizedTypeName.get(collection, ClassName.get(Boolean.class));
    private final ParameterizedTypeName collectionOfCharacters = ParameterizedTypeName.get(collection, ClassName.get(Character.class));

    public DataClassMetaGenerator() {
    }

    public TypeSpec generate(TupleMeta tupleMeta) {
        return TypeSpec.classBuilder(tupleMeta.className + "Meta")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(Meta.class), tupleMeta.classType))
                .addMethod(generateDataClassToListMethod(tupleMeta))
                .addMethod(generateListToDataClassMethod(tupleMeta))
                .build();
    }

    private MethodSpec generateListToDataClassMethod(TupleMeta tupleMeta) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("fromList")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(wildCardList, "values", Modifier.FINAL)
                .returns(tupleMeta.classType)
                .addStatement("$T result = new $T()", tupleMeta.classType, tupleMeta.classType);

        for (FieldMeta fieldMeta : tupleMeta.fields) {
            if (Common.isNumber(fieldMeta.field.asType().getKind())) {
                builder.addCode(handleNumberType(fieldMeta));
            } else if (fieldMeta.field.asType().getKind() == TypeKind.ARRAY) {
                builder.addCode(handleArrayType(fieldMeta));
            } else {
                builder.addCode(handleDeclaredType(fieldMeta));
            }
        }

        builder.addStatement("return result");

        return builder.build();
    }

    private CodeBlock handleDeclaredType(FieldMeta fieldMeta) {
        return CodeBlock
                .builder()
                .addStatement("result.$L(($T) values.get($L))", fieldMeta.setterName, fieldMeta.valueType, fieldMeta.getIndex())
                .build();
    }

    private CodeBlock handleNumberType(FieldMeta fieldMeta) {
        String toValue = fieldMeta.field.asType().getKind().name().toLowerCase() + "Value()";

        return CodeBlock
                .builder()
                .addStatement("result.$L((($T) values.get($L)).$L)", fieldMeta.setterName, Number.class, fieldMeta.getIndex(), toValue)
                .build();
    }

    private CodeBlock handleArrayType(FieldMeta fieldMeta) {
        CodeBlock.Builder builder = CodeBlock.builder();

        TypeMirror arrayTypeKind = ((ArrayType) fieldMeta.field.asType()).getComponentType();
        if (arrayTypeKind.getKind().isPrimitive()) {
            builder.add(handlePrimitiveArrayType(arrayTypeKind.getKind(), fieldMeta.setterName, fieldMeta.getIndex()));
        } else {
            builder.addStatement("result.$L((($T) values.get($L)).toArray(new $T {}))", fieldMeta.setterName, wildCardList, fieldMeta.getIndex(), fieldMeta.field.asType());
        }

        return builder.build();
    }

    private CodeBlock handlePrimitiveArrayType(TypeKind kind, String setterName, int index) {
        CodeBlock.Builder builder = CodeBlock.builder();

        switch (kind) {
            case BOOLEAN:
                builder.addStatement("result.$L($T.toArray(($T) values.get($L)))", setterName, Booleans.class, collectionOfBooleans, index);
                break;
            case BYTE:
                builder.addStatement("result.$L($T.toArray(($T) values.get($L)))", setterName, Bytes.class, subtypeOfNumber, index);
                break;
            case SHORT:
                builder.addStatement("result.$L($T.toArray(($T) values.get($L)))", setterName, Shorts.class, subtypeOfNumber, index);
                break;
            case INT:
                builder.addStatement("result.$L($T.toArray(($T) values.get($L)))", setterName, Ints.class, subtypeOfNumber, index);
                break;
            case LONG:
                builder.addStatement("result.$L($T.toArray(($T) values.get($L)))", setterName, Longs.class, subtypeOfNumber, index);
                break;
            case CHAR:
                builder.addStatement("result.$L($T.toArray(($T) values.get($L)))", setterName, Chars.class, collectionOfCharacters, index);
                break;
            case FLOAT:
                builder.addStatement("result.$L($T.toArray(($T) values.get($L)))", setterName, Floats.class, subtypeOfNumber, index);
                break;
            case DOUBLE:
                builder.addStatement("result.$L($T.toArray(($T) values.get($L)))", setterName, Doubles.class, subtypeOfNumber, index);
                break;
        }

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
}
