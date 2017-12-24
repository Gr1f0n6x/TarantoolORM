package org.tarantool.orm.type;

/**
 * Created by GrIfOn on 20.12.2017.
 */
public enum IteratorType {
    EQ(0, "EQ"),
    REQ(1, "REQ"),
    GT(2, "GT"),
    GE(3, "GE"),
    ALL(4, "ALL"),
    LT(5, "LT"),
    LE(6, "LE"),
    BITS_ALL_SET(7, ""),
    BITS_ANY_SET(8, ""),
    BITS_ALL_NOT_SET(9, ""),
    OVERLAPS(10, "OVERLAPS"),
    NEIGHBOR(11, "NEIGHBOR");

    private final int type;
    private final String name;

    IteratorType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
