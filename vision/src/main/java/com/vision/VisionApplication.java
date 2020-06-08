package com.vision;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableScheduling
@SpringBootApplication
public class VisionApplication {
	@Autowired
	public static ApplicationContext appContext;
	@Autowired
	DataSource datasource;
	
	public static void main(String[] args) {
		appContext = SpringApplication.run(VisionApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext appContext) {
		this.appContext = appContext;
		
		/*long ut1 = Instant.now().getEpochSecond();
        Date dNow = new Date(ut1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dNow);
        cal.add(Calendar.MINUTE, 5);
        dNow = cal.getTime();*/

        
		return args -> {
//			String[] beans = appContext.getBeanDefinitionNames();
//			Arrays.stream(beans).sorted().forEach(System.out::println);
		};
	}
	
	@Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
            	/*registry.addMapping("/**").allowedOrigins("http://10.16.1.155:4200");*/
            	/*registry.addMapping("/**").allowedOrigins("http://10.212.134.200:4200");*/
            	registry.addMapping("/**")
            	.allowedOrigins("*")
                .allowedHeaders(CrossOrigin.DEFAULT_ALLOWED_HEADERS)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .maxAge(3600L);
            }
        };
    }

}

