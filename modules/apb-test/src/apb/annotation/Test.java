package apb.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;

/**
 * An annotation used to define tests groups
 * User: diegor
 * Date: Apr 30, 2009
 * Time: 9:47:47 AM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(METHOD)

public @interface Test {
    /** Specifies test group */
    String group() default "slow";
    boolean skip() default false;

}
