package org.tarantool.orm.auto;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

final class IndexFieldMeta {
    public final VariableElement indexField;
    public final String indexName;
    public final TypeName fieldType;
    public final ExecutableElement getter;
    public final String getterName;
    public final ExecutableElement setter;
    public final String setterName;
    public final String fieldName;
    public final int part;
    public final boolean isNullable;

    public static IndexFieldMeta getInstance(VariableElement element, ExecutableElement getter, ExecutableElement setter, String indexName, int part, boolean isNullable) {
        // Math.max(part, 1) -> if part < 0 then use 1 else part
        return new IndexFieldMeta(element, getter, setter, indexName, Math.max(part, 1), isNullable);
    }

    private IndexFieldMeta(VariableElement element, ExecutableElement getter, ExecutableElement setter, String indexName, int part, boolean isNullable) {
        this.indexField = element;
        this.indexName = indexName;
        this.fieldType = TypeName.get(element.asType());
        this.fieldName = indexField.getSimpleName().toString();
        this.part = part;
        this.isNullable = isNullable;
        this.getter = getter;
        this.getterName = getter.getSimpleName().toString();
        this.setter = setter;
        this.setterName = setter.getSimpleName().toString();
    }
}
