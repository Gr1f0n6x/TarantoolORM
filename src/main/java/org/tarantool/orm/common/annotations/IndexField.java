package org.tarantool.orm.common.annotations;

import org.tarantool.orm.common.type.TarantoolType;

import java.lang.annotation.*;

/**
 * Created by GrIfOn on 20.12.2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface IndexField {
    String indexName();
    int part() default 1;
    TarantoolType type() default TarantoolType.SCALAR;
}
