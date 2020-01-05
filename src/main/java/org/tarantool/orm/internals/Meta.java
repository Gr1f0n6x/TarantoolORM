package org.tarantool.orm.internals;

import java.util.List;

public abstract class Meta<T> {
    public abstract List<?> toList(T value);

    public abstract T fromList(List<?> values);

    // values -> List of List<?>
    public final T resultToDataClass(List<?> values) {
        if (values.size() == 1) {
            return fromList(((List<?>) values.get(0)));
        } else {
            return null;
        }
    }
}
