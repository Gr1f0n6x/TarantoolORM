package org.tarantool.orm.auto;

import com.squareup.javapoet.TypeName;
import org.tarantool.orm.annotations.Field;
import org.tarantool.orm.annotations.IndexedField;
import org.tarantool.orm.annotations.IndexedFieldParams;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class FieldMeta {
    public final VariableElement field;
    public final TypeName fieldType;
    public final ExecutableElement getter;
    public final String getterName;
    public final ExecutableElement setter;
    public final String setterName;
    public final String fieldName;
    // position is the index of field specified by user, so it may be not unique. Represents a desired field position
    public final int position;
    public final boolean isIndexed;
    public final List<IndexFieldMeta> indexFieldMetas;
    public final TypeMirror valueType;

    private int index;

    public static FieldMeta getInstance(VariableElement element, ExecutableElement getter, ExecutableElement setter, Types typeUtil) {
        return new FieldMeta(element, getter, setter, typeUtil);
    }

    private FieldMeta(VariableElement variableElement, ExecutableElement getter, ExecutableElement setter, Types typeUtil) {
        this.field = variableElement;
        this.fieldName = variableElement.getSimpleName().toString();
        this.fieldType = TypeName.get(variableElement.asType());
        this.getter = getter;
        this.getterName = getter.getSimpleName().toString();
        this.setter = setter;
        this.setterName = setter.getSimpleName().toString();

        TypeMirror valueType = variableElement.asType();
        if (variableElement.asType().getKind().isPrimitive()) {
            valueType = typeUtil.boxedClass((PrimitiveType) variableElement.asType()).asType();
        }

        this.valueType = valueType;

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
                indexFieldMetas.add(IndexFieldMeta.getInstance(variableElement, getter, setter, params.indexName(), params.part(), params.isNullable()));
            }

            this.indexFieldMetas = Collections.unmodifiableList(indexFieldMetas);
        } else {
            this.isIndexed = false;
            this.indexFieldMetas = Collections.unmodifiableList(Collections.emptyList());
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
