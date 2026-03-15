package one.theone.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TheOneApplication {

    public static void main(String[] args) {
        SpringApplication.run(TheOneApplication.class, args);
    }

}
