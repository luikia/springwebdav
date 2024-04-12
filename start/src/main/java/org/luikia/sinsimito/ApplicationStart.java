package org.luikia.sinsimito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.luikia.sinsimito"})
public class ApplicationStart {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationStart.class, args);
    }


}
