package com.project.bookmyshow.server;

import com.project.bookmyshow.db.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ComponentScan("com.project.bookmyshow")
public class BookMyShowApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(BookMyShowApplication.class, args);
        try {
            ConnectionFactory.INSTANCE.init();
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            // Shutdown Application
            applicationContext.close();
        }
    }
}
