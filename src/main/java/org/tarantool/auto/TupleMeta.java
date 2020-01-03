package org.tarantool.auto;


import org.tarantool.orm.annotations.Tuple;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class TupleMeta {
    public final TypeElement classElement;
    public final List<FieldMeta> fields;
    public final Map<String, IndexMeta> indexMetas;
    public final String className;
    public final String initialClassName;
    public final String spaceName;

    public static TupleMeta getInstance(TypeElement element) {
        isClassValid(element);
        List<FieldMeta> fieldMetas = getFieldMetas(element);

        return new TupleMeta(element, fieldMetas);
    }

    private static void isClassValid(TypeElement element) {
        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            throw new IllegalArgumentException(String.format("Class %s should be public", element.getSimpleName()));
        }

        if (element.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new IllegalArgumentException(String.format("Class %s should not be abstract", element.getSimpleName()));
        }
    }

    private static List<FieldMeta> getFieldMetas(TypeElement element) {
        Map<String, ExecutableElement> executableElementMap = getMethodsMap(element);
        List<FieldMeta> fieldMetas = new ArrayList<>();

        for (Element el : element.getEnclosedElements()) {
            if (el.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) el;
                TypeKind fieldKind = el.asType().getKind();
                ExecutableElement getter;
                ExecutableElement setter;

                String fieldName = el.getSimpleName().toString();
                String capitalizedFieldName = Common.capitalize(fieldName);

                if (fieldKind == TypeKind.BOOLEAN) {
                    getter = executableElementMap.get("is" + capitalizedFieldName);
                } else {
                    getter = executableElementMap.get("get" + capitalizedFieldName);
                }
                setter = executableElementMap.get("set" + capitalizedFieldName);

                if (getter == null) {
                    throw new IllegalArgumentException(String.format("Field %s in class %s has no getter", fieldName, element.getSimpleName()));
                }

                if (setter == null) {
                    throw new IllegalArgumentException(String.format("Field %s in class %s has no setter", fieldName, element.getSimpleName()));
                }

                if (getter.getReturnType() != field.asType()) {
                    throw new IllegalArgumentException(String.format("Field %s in class %s has getter with incorrect return type", fieldName, element.getSimpleName()));
                }

                if (setter.getParameters().size() != 1 || setter.getParameters().contains(field)) {
                    throw new IllegalArgumentException(String.format("Field %s in class %s has setter with incorrect parameter type", fieldName, element.getSimpleName()));
                }

                fieldMetas.add(FieldMeta.getInstance(field, getter, setter));
            }
        }

        if (fieldMetas.isEmpty()) {
            throw new IllegalArgumentException(String.format("Class %s has no fields", element.getSimpleName()));
        }

        return fieldMetas;
    }

    private static Map<String, ExecutableElement> getMethodsMap(TypeElement element) {
        Map<String, ExecutableElement> executableElementMap = new HashMap<>();

        for (Element el : element.getEnclosedElements()) {
            if (el.getKind() == ElementKind.METHOD) {
                ExecutableElement executableElement = (ExecutableElement) el;
                executableElementMap.put(executableElement.getSimpleName().toString(), executableElement);
            }
        }

        return executableElementMap;
    }

    private TupleMeta(TypeElement classElement, List<FieldMeta> fields) {
        this.classElement = classElement;
        this.fields = Collections.unmodifiableList(fields);
        this.initialClassName = classElement.getSimpleName().toString();
        this.className = classElement.getSimpleName().toString() + "Manager";

        Tuple tupleAnnotation = classElement.getAnnotation(Tuple.class);
        this.spaceName = tupleAnnotation.spaceName();

        if (this.spaceName.isEmpty()) {
            throw new IllegalArgumentException("Space name should not be empty");
        }

        if (tupleAnnotation.indexes().length == 0) {
            throw new IllegalArgumentException(String.format("Tuple %s must have at least one index", initialClassName));
        }

        Map<String, IndexMeta> indexMetaMap;

        try {
            indexMetaMap = Stream.of(tupleAnnotation.indexes())
                    .map(IndexMeta::getInstance)
                    .collect(Collectors.toMap(meta -> meta.name, Function.identity()));
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("Index names should be unique");
        }

        long primaryIndexes = indexMetaMap.values().stream().filter(meta -> meta.isPrimary).count();

        if (primaryIndexes == 0) {
            throw new IllegalArgumentException(String.format("Tuple %s does not have primary index", initialClassName));
        }

        if (primaryIndexes > 1) {
            throw new IllegalArgumentException("Only one index may be primary");
        }

        this.indexMetas = Collections.unmodifiableMap(indexMetaMap);
    }
}
