package ru.ifmo.movies_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
public class MoviesAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviesAppApplication.class, args);
	}

}
