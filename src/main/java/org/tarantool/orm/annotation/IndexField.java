package org.tarantool.orm.annotation;

import org.tarantool.orm.type.TarantoolType;

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
