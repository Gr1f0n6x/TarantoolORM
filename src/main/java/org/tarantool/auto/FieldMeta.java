package org.tarantool.auto;

import javax.lang.model.element.VariableElement;

final class FieldMeta {
    private VariableElement field;
    private String fieldName;

    public static FieldMeta getInstance(VariableElement element) {
        return new FieldMeta(element);
    }

    public FieldMeta(VariableElement field) {
        this.field = field;
        this.fieldName = field.getSimpleName().toString();
    }
}
