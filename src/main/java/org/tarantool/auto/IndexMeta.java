package org.tarantool.auto;

import org.tarantool.orm.annotations.Index;

import java.util.Objects;

final class IndexMeta {
    public final String name;
    public final boolean isPrimary;

    public static IndexMeta getInstance(Index index) {
        return new IndexMeta(index.name(), index.isPrimary());
    }

    private IndexMeta(String name, boolean isPrimary) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Index name should not be empty");
        }

        this.name = name;
        this.isPrimary = isPrimary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexMeta indexMeta = (IndexMeta) o;
        return Objects.equals(name, indexMeta.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
