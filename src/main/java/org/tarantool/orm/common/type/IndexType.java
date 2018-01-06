package org.tarantool.orm.common.type;

/**
 * Created by GrIfOn on 20.12.2017.
 */
public enum IndexType {
    HASH("hash"), TREE("tree"), RTREE("rtree"), BITSET("bitset");

    private final String type;

    IndexType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
