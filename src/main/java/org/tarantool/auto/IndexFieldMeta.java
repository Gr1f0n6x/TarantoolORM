package org.tarantool.auto;

import javax.lang.model.element.VariableElement;

public class IndexFieldMeta {
    public final VariableElement tupleField;
    public final String indexName;
    public final int part;
    public final boolean isNullable;

    public static IndexFieldMeta getInstance(VariableElement tupleField, String indexName, int part, boolean isNullable) {
        return new IndexFieldMeta(tupleField, indexName, part, isNullable);
    }

    public IndexFieldMeta(VariableElement tupleField, String indexName, int part, boolean isNullable) {
        this.tupleField = tupleField;
        this.indexName = indexName;
        this.part = part;
        this.isNullable = isNullable;
    }
}
