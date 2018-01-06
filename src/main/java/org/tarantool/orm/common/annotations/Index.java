package org.tarantool.orm.common.annotations;

import org.tarantool.orm.common.type.CollationType;
import org.tarantool.orm.common.type.IndexType;

import java.lang.annotation.*;

/**
 * Created by GrIfOn on 04.01.2018.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Index {
    String name();
    IndexType type() default IndexType.TREE;
    boolean unique() default false;
    boolean ifNotExists() default false;
    CollationType collationType() default CollationType.BINARY;
}
