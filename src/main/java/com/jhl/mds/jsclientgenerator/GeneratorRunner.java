package com.jhl.mds.jsclientgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
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
        ConfigurableApplicationContext ctx = SpringApplication.run(JsClientGenerator.class);
        ctx.close();
    }
}
