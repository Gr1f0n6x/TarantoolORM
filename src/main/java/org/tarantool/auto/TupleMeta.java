package org.tarantool.auto;


import org.tarantool.orm.annotations.Tuple;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TupleMeta {
    private final TypeElement classElement;
    public final List<FieldMeta> fields;
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

    public TupleMeta(TypeElement classElement, List<FieldMeta> fields) {
        this.classElement = classElement;
        this.fields = Collections.unmodifiableList(fields);
        this.initialClassName = classElement.getSimpleName().toString();
        this.className = classElement.getSimpleName().toString() + "Manager";

        Tuple tupleAnnotation = classElement.getAnnotation(Tuple.class);
        this.spaceName = tupleAnnotation.spaceName();

        if (this.spaceName.isEmpty()) {
            throw new IllegalArgumentException("Space name should not be empty");
        }
    }
}
