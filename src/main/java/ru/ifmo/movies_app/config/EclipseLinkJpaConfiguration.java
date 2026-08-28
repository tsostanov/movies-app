package ru.ifmo.movies_app.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.eclipse.persistence.jpa.PersistenceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
public class EclipseLinkJpaConfiguration {

    @Value("${spring.jpa.properties.eclipselink.logging.level:INFO}")
    private String loggingLevel;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPersistenceProviderClass(PersistenceProvider.class);
        emf.setPackagesToScan("ru.ifmo.movies_app");

        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.schema-generation.database.action", "none");
        props.put("eclipselink.weaving", "false");
        props.put("eclipselink.logging.level", loggingLevel);
        emf.setJpaPropertyMap(props);

        emf.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
