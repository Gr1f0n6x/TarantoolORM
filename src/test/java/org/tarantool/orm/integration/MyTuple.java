package org.tarantool.orm.integration;

import org.tarantool.orm.annotations.Index;
import org.tarantool.orm.annotations.IndexedField;
import org.tarantool.orm.annotations.IndexedFieldParams;
import org.tarantool.orm.annotations.Tuple;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@Tuple(spaceName = "test", indexes = {
        @Index(name = "primary", isPrimary = true),
        @Index(name = "secondary")
})
public class MyTuple {
    @IndexedField(indexes = @IndexedFieldParams(indexName = "primary"))
    private int f1;
    @IndexedField(indexes = @IndexedFieldParams(indexName = "secondary"))
    private String f2;
    private short f3;
    private byte f4;
    private long f5;
    private float f6;
    private double f7;
    private long[] f8;
    private boolean f9;
    private Object[] f10;
    private Map<String, Object> f11;

    public int getF1() {
        return f1;
    }

    public void setF1(int f1) {
        this.f1 = f1;
    }

    public String getF2() {
        return f2;
    }

    public void setF2(String f2) {
        this.f2 = f2;
    }

    public short getF3() {
        return f3;
    }

    public void setF3(short f3) {
        this.f3 = f3;
    }

    public byte getF4() {
        return f4;
    }

    public void setF4(byte f4) {
        this.f4 = f4;
    }

    public long getF5() {
        return f5;
    }

    public void setF5(long f5) {
        this.f5 = f5;
    }

    public float getF6() {
        return f6;
    }

    public void setF6(float f6) {
        this.f6 = f6;
    }

    public double getF7() {
        return f7;
    }

    public void setF7(double f7) {
        this.f7 = f7;
    }

    public long[] getF8() {
        return f8;
    }

    public void setF8(long[] f8) {
        this.f8 = f8;
    }

    public boolean isF9() {
        return f9;
    }

    public void setF9(boolean f9) {
        this.f9 = f9;
    }

    public Object[] getF10() {
        return f10;
    }

    public void setF10(Object[] f10) {
        this.f10 = f10;
    }

    public Map<String, Object> getF11() {
        return f11;
    }

    public void setF11(Map<String, Object> f11) {
        this.f11 = f11;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyTuple tuple = (MyTuple) o;
        return f1 == tuple.f1 &&
                f3 == tuple.f3 &&
                f4 == tuple.f4 &&
                f5 == tuple.f5 &&
                Float.compare(tuple.f6, f6) == 0 &&
                Double.compare(tuple.f7, f7) == 0 &&
                f9 == tuple.f9 &&
                Objects.equals(f2, tuple.f2) &&
                Arrays.equals(f8, tuple.f8) &&
                Arrays.equals(f10, tuple.f10) &&
                Objects.equals(f11, tuple.f11);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(f1, f2, f3, f4, f5, f6, f7, f9, f11);
        result = 31 * result + Arrays.hashCode(f8);
        result = 31 * result + Arrays.hashCode(f10);
        return result;
    }

    @Override
    public String toString() {
        return "MyTuple{" +
                "f1=" + f1 +
                ", f2='" + f2 + '\'' +
                ", f3=" + f3 +
                ", f4=" + f4 +
                ", f5=" + f5 +
                ", f6=" + f6 +
                ", f7=" + f7 +
                ", f8=" + Arrays.toString(f8) +
                ", f9=" + f9 +
                ", f10=" + Arrays.toString(f10) +
                ", f11=" + f11 +
                '}';
    }
}
