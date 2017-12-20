package annotation;

import java.lang.annotation.*;

/**
 * Created by GrIfOn on 20.12.2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface IndexField {
    String indexName();
    int parts() default 1;
}
