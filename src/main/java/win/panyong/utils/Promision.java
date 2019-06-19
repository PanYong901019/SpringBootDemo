package win.panyong.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by pan on 2019/2/12 11:14 AM
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Promision {
    /*
        注解参数根据PromisionType填写,填写什么就说明授什么权 可以填写多个
     */
    PromisionType[] value() default PromisionType.ADMIN;
}
