package org.tarantool.orm.auto;


import com.google.common.collect.Sets;
import com.squareup.javapoet.TypeName;
import org.tarantool.orm.annotations.Tuple;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class TupleMeta {
    public final TypeElement classElement;
    public final TypeName classType;
    public final List<FieldMeta> fields;
    public final Map<String, List<IndexFieldMeta>> indexedFields;
    public final String primaryIndexName;
    public final Map<String, IndexMeta> indexMetas;
    public final String className;
    public final String initialClassName;
    public final String spaceName;

    public static TupleMeta getInstance(TypeElement element, Types typeUtil) {
        isClassValid(element);
        List<FieldMeta> fieldMetas = getFieldMetas(element, typeUtil);
        Map<String, List<IndexFieldMeta>> indexedFields = getIndexedFields(fieldMetas);

        return new TupleMeta(element, fieldMetas, indexedFields);
    }

    private static void isClassValid(TypeElement element) {
        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            throw new IllegalArgumentException(String.format("Class %s should be public", element.getSimpleName()));
        }

        if (element.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new IllegalArgumentException(String.format("Class %s should not be abstract", element.getSimpleName()));
        }
    }

    private static List<FieldMeta> getFieldMetas(TypeElement element, Types typeUtil) {
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

                if (!typeUtil.isSameType(getter.getReturnType(), field.asType())) {
                    throw new IllegalArgumentException(String.format("Field %s in class %s has getter with incorrect return type", fieldName, element.getSimpleName()));
                }

                if (setter.getParameters().size() != 1 || !typeUtil.isSameType(setter.getParameters().get(0).asType(), field.asType())) {
                    throw new IllegalArgumentException(String.format("Field %s in class %s has setter with incorrect parameter type", fieldName, element.getSimpleName()));
                }

                fieldMetas.add(FieldMeta.getInstance(field, getter, setter, typeUtil));
            }
        }

        if (fieldMetas.isEmpty()) {
            throw new IllegalArgumentException(String.format("Class %s has no fields", element.getSimpleName()));
        }

        List<FieldMeta> sortedFieldList = fieldMetas
                .stream()
                .sorted(Comparator.comparingInt(meta -> meta.position))
                .collect(Collectors.toList());

        int index = 0;
        for (FieldMeta meta : sortedFieldList) {
            meta.setIndex(index++);
        }

        return sortedFieldList;
    }

    private static Map<String, List<IndexFieldMeta>> getIndexedFields(List<FieldMeta> fieldMetas) {
        List<FieldMeta> indexedFields = fieldMetas
                .stream()
                .filter(field -> field.isIndexed)
                .collect(Collectors.toList());

        if (indexedFields.isEmpty()) {
            throw new IllegalArgumentException("Data class must have at least one indexed field");
        }

        return indexedFields
                .stream()
                .flatMap(field -> field.indexFieldMetas.stream())
                .sorted(Comparator.comparingInt(value -> value.part))
                .collect(Collectors.groupingBy(
                        indexFieldMeta -> indexFieldMeta.indexName,
                        Collectors.mapping(Function.identity(), Collectors.toList())
                ));
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

    private TupleMeta(TypeElement classElement, List<FieldMeta> fields, Map<String, List<IndexFieldMeta>> indexedFields) {
        this.classElement = classElement;
        this.fields = Collections.unmodifiableList(fields);
        this.initialClassName = classElement.getSimpleName().toString();
        this.className = classElement.getSimpleName().toString() + "Manager";
        this.classType = TypeName.get(classElement.asType());
        this.indexedFields = Collections.unmodifiableMap(indexedFields);

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

        //noinspection OptionalGetWithoutIsPresent
        this.primaryIndexName = indexMetaMap.values().stream().filter(meta -> meta.isPrimary).findFirst().get().name;

        this.indexMetas = Collections.unmodifiableMap(indexMetaMap);

        Sets.SetView<String> difference = Sets.symmetricDifference(
                indexMetas.keySet(),
                indexedFields.keySet()
        );

        if (!difference.isEmpty()) {
            throw new IllegalArgumentException("Name of indexes in @IndexedField and in @Index must correspond to each other");
        }
    }
}
