package org.tarantool.auto;

public class IndexFieldMeta {
    public final String indexName;
    public final int part;
    public final boolean isNullable;

    public static IndexFieldMeta getInstance(String indexName, int part, boolean isNullable) {
        return new IndexFieldMeta(indexName, part, isNullable);
    }

    public IndexFieldMeta(String indexName, int part, boolean isNullable) {
        this.indexName = indexName;
        this.part = part;
        this.isNullable = isNullable;
    }
}
