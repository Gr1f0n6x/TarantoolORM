package org.tarantool.orm.common.annotations;

import java.lang.annotation.*;

/**
 * Created by GrIfOn on 03.01.2018.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Field {
    int position() default 1;
}
