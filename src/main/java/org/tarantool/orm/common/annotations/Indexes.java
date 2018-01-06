package org.tarantool.orm.common.annotations;

import java.lang.annotation.*;

/**
 * Created by GrIfOn on 05.01.2018.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Indexes {
    Index[] indexList();
}
