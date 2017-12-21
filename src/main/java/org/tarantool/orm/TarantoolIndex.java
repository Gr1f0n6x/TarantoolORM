package org.tarantool.orm;

import org.tarantool.orm.annotation.IndexField;
import org.tarantool.orm.type.IndexType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by GrIfOn on 21.12.2017.
 */
final public class TarantoolIndex {
    private String name;
    private IndexType type;
    private boolean ifNotExists;
    private boolean unique;

    public TarantoolIndex(String name, IndexType type) {
        this(name, type, false);
    }

    public TarantoolIndex(String name, IndexType type, boolean ifNotExists) {
        this(name, type, ifNotExists, true);
    }

    public TarantoolIndex(String name, IndexType type, boolean ifNotExists, boolean unique) {
        this.name = name;
        this.type = type;
        this.ifNotExists = ifNotExists;
        this.unique = unique;
    }

    public String getName() {
        return name;
    }

    public String createIndex(String spaceName, List<IndexField> indexFields) {
        return String.format("box.space.%s:create_index('%s', {type='%s', if_not_exists=%s, unique=%s, parts={%s}})",
                spaceName,
                this.name,
                this.type.getType(),
                this.ifNotExists,
                this.unique,
                String.join(", ", indexFields.stream().map(x -> String.format("%d, '%s'", x.part(), x.type())).collect(Collectors.toList()))
        );
    }

    public String createIndex(int spaceId, List<IndexField> indexFields) {
        return String.format("box.space[%d]:create_index('%s', {type='%s', if_not_exists=%s, unique=%s, parts={%s}})",
                spaceId,
                this.name,
                this.type.getType(),
                this.ifNotExists,
                this.unique,
                String.join(", ", indexFields.stream().map(x -> String.format("%d, '%s'", x.part(), x.type())).collect(Collectors.toList()))
        );
    }
}
