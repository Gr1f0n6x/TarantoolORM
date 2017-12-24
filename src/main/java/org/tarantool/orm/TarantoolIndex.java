package org.tarantool.orm;

import org.tarantool.orm.annotation.IndexField;
import org.tarantool.orm.type.IndexType;
import org.tarantool.orm.type.IteratorType;

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

    private List<IndexField> indexFields;

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

    public String parts() {
        return this.indexFields
                .stream()
                .map(x -> String.format("IndexField{%d, %s}", x.part(), x.type().getType()))
                .collect(Collectors.toList())
                .toString();
    }

    public String getName() {
        return name;
    }

    public String createIndex(String spaceName, List<IndexField> indexFields) {
        this.indexFields = indexFields;

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
        this.indexFields = indexFields;

        return String.format("box.space[%d]:create_index('%s', {type='%s', if_not_exists=%s, unique=%s, parts={%s}})",
                spaceId,
                this.name,
                this.type.getType(),
                this.ifNotExists,
                this.unique,
                String.join(", ", indexFields.stream().map(x -> String.format("%d, '%s'", x.part(), x.type())).collect(Collectors.toList()))
        );
    }

    public String dropIndex(String spaceName) {
        return String.format("box.space.%s.index.%s:drop()", spaceName, this.name);
    }

    public String dropIndex(int spaceId) {
        return String.format("box.space[%d].index.%s:drop()", spaceId, this.name);
    }

    public String min(String spaceName) {
        return String.format("return box.space.%s.index.%s:min()", spaceName, this.name);
    }

    public String min(int spaceId) {
        return String.format("return box.space[%d].index.%s:min()", spaceId, this.name);
    }

    public String max(String spaceName) {
        return String.format("return box.space.%s.index.%s:max()", spaceName, this.name);
    }

    public String max(int spaceId) {
        return String.format("return box.space[%d].index.%s:max()", spaceId, this.name);
    }

    public String random(String spaceName, int seed) {
        return String.format("return box.space.%s.index.%s:random(%d)", spaceName, this.name, seed);
    }

    public String random(int spaceId, int seed) {
        return String.format("return box.space[%d].index.%s:random(%d)", spaceId, this.name, seed);
    }

    public String count(String spaceName, List<?> key) {
        return String.format("return box.space.%s.index.%s:count({%s})",
                spaceName,
                this.name,
                String.join(", ", key.stream().map(Object::toString).collect(Collectors.toList())));
    }

    public String count(int spaceId, List<?> key) {
        return String.format("return box.space[%d].index.%s:count({%s})",
                spaceId,
                this.name,
                String.join(", ", key.stream().map(Object::toString).collect(Collectors.toList())));
    }

    public String count(String spaceName, List<?> key, IteratorType type) {
        return String.format("return box.space.%s.index.%s:count({%s}, {iterator = '%s'})",
                spaceName,
                this.name,
                String.join(", ", key.stream().map(Object::toString).collect(Collectors.toList())),
                type.getName());
    }

    public String count(int spaceId, List<?> key, IteratorType type) {
        return String.format("return box.space[%d].index.%s:count({%s}, {iterator = '%s'})",
                spaceId,
                this.name,
                String.join(", ", key.stream().map(Object::toString).collect(Collectors.toList())),
                type.getName());
    }

    public String bsize(String spaceName) {
        return String.format("return box.space.%s.index.%s:bsize()", spaceName, this.name);
    }

    public String bsize(int spaceId) {
        return String.format("return box.space[%d].index.%s:bsize()", spaceId, this.name);
    }

    public String alter(String spaceName, boolean unique, IndexType type) {
        return String.format("return box.space.%s.index.%s:alter({type = '%s', unique = %s})", spaceName, this.name, type.getType(), unique);
    }

    public String alter(int spaceId, boolean unique, IndexType type) {
        return String.format("return box.space[%d].index.%s:alter({type = '%s', unique = %s})", spaceId, this.name, type.getType(), unique);
    }
}
