package com.ptpt.authservice.config;

import com.ptpt.authservice.entity.EntityModule;
import com.ptpt.authservice.repository.RepositoryModule;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

// 전체 엔티티 스캔과 레파지토리를 스캔하는 것이 아니라
// Module 이 포함된 클래스만 스캔을 하겠다라는 의미이다.
// 즉, 범위를 지정하는 것임.
@Configuration
@EntityScan(basePackageClasses = {EntityModule.class})
@EnableJpaRepositories(basePackageClasses = {RepositoryModule.class})
public class PersistenceJpaConfig {
//    data source 에 대한 설정

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

//    @Bean
//    public PlatformTransactionManager transactionManager(DataSource dataSource) {
////        jdbc transaction manager 는 datasource 를 설정하지 않으면 오류가 발생한다.
//        JdbcTransactionManager jdbcTransactionManager = new JdbcTransactionManager();
//        jdbcTransactionManager.setDataSource(dataSource);
//        return jdbcTransactionManager;
//    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager();
    }

    @Bean
    public TransactionTemplate writeTransactionOperations(PlatformTransactionManager platformTransactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.setReadOnly(false);
        return transactionTemplate;
    }

    @Bean
    public TransactionTemplate readTransactionOperations(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(true);
        return transactionTemplate;
    }

}
