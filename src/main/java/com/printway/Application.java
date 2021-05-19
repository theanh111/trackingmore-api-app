package com.printway;

import com.printway.api.TrackingmoreAPI;
import com.printway.config.DBConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@SpringBootApplication
@AutoConfigurationPackage
@PropertySources({@PropertySource("classpath:application.properties")})
public class Application {
    @Autowired
    private Environment env;

    @Bean(name = "dataSource")
    public DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        return dataSource;
    }

    public static void main(String[] args) {
        try {
            System.out.println("Tracking App Running...");
            System.out.println("*** SQL Connection: " + DBConnection.getInstance().getConnection().toString().split("@")[1]);
            ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
            TrackingmoreAPI trackingmoreAPI = new TrackingmoreAPI();
            trackingmoreAPI.getTrackingInfo();
            System.out.println("** DONE **");
            //            System.out.println("- SQL Connection: " + DBConnection.getInstance().getConnection().toString().split("@")[1]);
            DBConnection.getInstance().closeConnection();
            int exitCode = SpringApplication.exit(ctx, new ExitCodeGenerator() {
                @Override
                public int getExitCode() {
                    return 0;
                }
            });
            System.exit(exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
            //            System.out.println("- SQL Connection: " + DBConnection.getInstance().getConnection().toString().split("@")[1]);
            DBConnection.getInstance().closeConnection();
            int exitCode = SpringApplication.exit(ctx, new ExitCodeGenerator() {
                @Override
                public int getExitCode() {
                    return 0;
                }
            });
            System.exit(exitCode);
        }
    }
}
