package org.tarantool.auto;


import org.tarantool.orm.annotations.Tuple;

import javax.lang.model.element.*;
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
        List<FieldMeta> fieldMetas = new ArrayList<>();

        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            throw new IllegalArgumentException(String.format("Class %s should be public", element.getSimpleName()));
        }

        if (element.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new IllegalArgumentException(String.format("Class %s should not be abstract", element.getSimpleName()));
        }

        for (Element el : element.getEnclosedElements()) {
            if (el.getKind() == ElementKind.FIELD) {
                fieldMetas.add(FieldMeta.getInstance((VariableElement) el));
            }
        }

        if (fieldMetas.isEmpty()) {
            throw new IllegalArgumentException(String.format("Class %s has no fields", element.getSimpleName()));
        }

        return new TupleMeta(element, fieldMetas);
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
