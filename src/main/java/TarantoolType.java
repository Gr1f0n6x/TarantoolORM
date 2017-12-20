/**
 * Created by GrIfOn on 21.12.2017.
 */
public enum TarantoolType {
    UNSIGNED("unsigned"),
    STRING("STR"),
    INTEGER("integer"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    ARRAY("array"),
    SCALAR("scalar");

    private final String type;

    TarantoolType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
