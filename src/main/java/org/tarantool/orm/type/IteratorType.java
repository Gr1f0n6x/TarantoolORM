package org.tarantool.orm.type;

/**
 * Created by GrIfOn on 20.12.2017.
 */
public enum IteratorType {
    EQ(0),
    REQ(1),
    GT(2),
    GE(3),
    ALL(4),
    LT(5),
    LE(6),
    BITS_ALL_SET(7),
    BITS_ANY_SET(8),
    BITS_ALL_NOT_SET(9),
    OVERLAPS(10),
    NEIGHBOR(11);

    private final int type;

    IteratorType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
