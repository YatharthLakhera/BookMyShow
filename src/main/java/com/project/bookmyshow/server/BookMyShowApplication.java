package com.project.bookmyshow.server;

import com.project.bookmyshow.db.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.DriverManager;

@Slf4j
@SpringBootApplication
@ComponentScan("com.project.bookmyshow")
public class BookMyShowApplication implements CommandLineRunner {

    @Autowired
    private DataSource datasource;

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

    @Override
    public void run(String... args) throws Exception {
        String script = System.getProperty("user.dir") + "/src/main/resources/MovieBooking.sql";
        ScriptRunner scriptRunner = new ScriptRunner(
                DriverManager.getConnection("jdbc:mysql://localhost:3306/movie_booking", "root", "ubona123"));
        scriptRunner.runScript(new BufferedReader(new FileReader(script)));
    }
}
