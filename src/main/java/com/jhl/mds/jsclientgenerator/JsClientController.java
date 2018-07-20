package com.jhl.mds.jsclientgenerator;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsClientController {
    String fileName();
    String className();
}
