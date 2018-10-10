package com.jhl.mds.jsclientgenerator;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Profile("js-generator")
public class GeneratorRunner {

    private JsClientGenerator jsClientGenerator;
    private JsJsDTOClassGenerator jsDTOClassGenerator;

    public GeneratorRunner(
            JsClientGenerator jsClientGenerator,
            JsJsDTOClassGenerator jsDTOClassGenerator
    ) {
        this.jsClientGenerator = jsClientGenerator;
        this.jsDTOClassGenerator = jsDTOClassGenerator;
    }

    @PostConstruct
    public void init() throws Exception {
        jsDTOClassGenerator.start();
        jsClientGenerator.start();
    }

    public static void main(String[] args) {
        args = ArrayUtils.add(args, "--spring.profiles.active=js-generator");
        ConfigurableApplicationContext ctx = SpringApplication.run(JsClientGenerator.class, args);
        ctx.close();
    }
}
