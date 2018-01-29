package org.tarantool.orm.common.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface IndexFieldParams {
    String indexName();
    boolean isNullable() default false;
}
