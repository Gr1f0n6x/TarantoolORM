package org.tarantool.auto;

import org.tarantool.orm.annotations.Field;
import org.tarantool.orm.annotations.IndexedField;
import org.tarantool.orm.annotations.IndexedFieldParams;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class FieldMeta {
    public final VariableElement tupleField;
    public final ExecutableElement getter;
    public final String getterName;
    public final ExecutableElement setter;
    public final String setterName;
    public final String fieldName;
    public final int position;
    public final boolean isIndexed;
    public final List<IndexFieldMeta> indexFieldMetas;

    public static FieldMeta getInstance(VariableElement element, ExecutableElement getter, ExecutableElement setter) {
        return new FieldMeta(element, getter, setter);
    }

    private FieldMeta(VariableElement variableElement, ExecutableElement getter, ExecutableElement setter) {
        this.tupleField = variableElement;
        this.fieldName = variableElement.getSimpleName().toString();
        this.getter = getter;
        this.getterName = getter.getSimpleName().toString();
        this.setter = setter;
        this.setterName = setter.getSimpleName().toString();

        Field field = variableElement.getAnnotation(Field.class);
        if (field == null) {
            this.position = 1;
        } else {
            this.position = field.position() > 0 ? field.position() : 1;
        }

        IndexedField indexField = variableElement.getAnnotation(IndexedField.class);
        if (indexField != null) {
            this.isIndexed = true;

            List<IndexFieldMeta> indexFieldMetas = new ArrayList<>();
            for (IndexedFieldParams params : indexField.indexes()) {
                indexFieldMetas.add(IndexFieldMeta.getInstance(variableElement, params.indexName(), params.part(), params.isNullable()));
            }

            this.indexFieldMetas = Collections.unmodifiableList(indexFieldMetas);
        } else {
            this.isIndexed = false;
            this.indexFieldMetas = Collections.unmodifiableList(Collections.emptyList());
        }
    }
}
