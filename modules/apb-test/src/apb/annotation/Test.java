package apb.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Created by IntelliJ IDEA.
 * User: diegor
 * Date: Apr 30, 2009
 * Time: 9:47:47 AM
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(METHOD)

public @interface Test {
    /** Specifies test group */
    String group() default "";

}
