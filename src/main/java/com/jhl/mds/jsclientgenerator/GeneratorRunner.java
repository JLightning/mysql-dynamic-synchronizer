package com.jhl.mds.jsclientgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;

public class GeneratorRunner {

    private JsClientGenerator jsClientGenerator;
    private JsDTOGenerator jsDTOGenerator;

    public GeneratorRunner(
            JsClientGenerator jsClientGenerator,
            JsDTOGenerator jsDTOGenerator
    ) {
        this.jsClientGenerator = jsClientGenerator;
        this.jsDTOGenerator = jsDTOGenerator;
    }

    @PostConstruct
    public void init() throws Exception {
        jsClientGenerator.start();
        jsDTOGenerator.start();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(JsClientGenerator.class);
        ctx.close();
    }
}
