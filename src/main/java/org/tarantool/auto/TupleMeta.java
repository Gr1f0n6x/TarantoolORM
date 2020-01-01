package org.tarantool.auto;


import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TupleMeta {
    public final TypeElement classElement;
    public final List<FieldMeta> fields;
    public final String className;
    public final String initialClassName;

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
    }
}
