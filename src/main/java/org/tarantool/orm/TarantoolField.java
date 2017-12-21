package org.tarantool.orm;

import java.io.Serializable;

/**
 * Created by GrIfOn on 20.12.2017.
 */
final public class TarantoolField<T extends Serializable> {
    private T value;

    public TarantoolField() {
        this(null);
    }

    public TarantoolField(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TarantoolField<?> that = (TarantoolField<?>) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
