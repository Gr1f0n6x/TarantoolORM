import java.io.Serializable;

/**
 * Created by GrIfOn on 20.12.2017.
 */
final public class TarantoolField<T extends Serializable> {
    private T value;
    private int part;
    private TarantoolType type;

    public TarantoolField() {
        this(null, TarantoolType.SCALAR, 1);
    }

    public TarantoolField(int part) {
        this(null, TarantoolType.SCALAR, part);
    }

    public TarantoolField(T value, int part) {
        this(value, TarantoolType.SCALAR, part);
    }

    public TarantoolField(T value, TarantoolType type, int part) {
        this.value = value;
        this.type = type;
        this.part = part;
    }

    public int getPart() {
        return part;
    }

    public T getValue() {
        return value;
    }

    public TarantoolType getType() {
        return type;
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

    final public String toIndexPart() {
        return this.part + ", '" + this.type.getType() + "'";
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
