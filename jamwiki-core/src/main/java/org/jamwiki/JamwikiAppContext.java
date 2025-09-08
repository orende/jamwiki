package org.jamwiki;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.jamwiki.db.AnsiDataHandler;
import org.jamwiki.db.DataHandler;
import org.jamwiki.db.DatabaseUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@Configuration
public class JamwikiAppContext {
    @Bean
    public Environment environment() {
        var environment = new Environment();
        // JFlexLexer is used by generated code, so cannot use normal dependency injection.
//        JFlexLexer.setEnvironment(environment);
        return environment;
    }

    @Bean
    public DataSource dataSource() throws Exception {
        Properties properties = new Properties();
        properties.putAll(Map.of(
                "username", "sa",
                "password", "",
                "url", "jdbc:hsqldb:file:/database/jamwikidb;shutdown=true",
                "driverClassName", "org.jamwiki.db.HSqlQueryHandler"
        ));
        return BasicDataSourceFactory.createDataSource(properties);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public WikiConfiguration wikiConfiguration() {
        return new WikiConfiguration();
    }

    @Bean
    public WikiBase wikiBase(DataHandler dataHandler, WikiConfiguration wikiConfiguration) throws IOException {
        return new WikiBase(dataHandler, wikiConfiguration);
    }

    @Bean
    public DatabaseUtils databaseUtils(DataHandler dataHandler) {
        return new DatabaseUtils();
    }

    @Bean
    public DataHandler dataHandler(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        return new AnsiDataHandler(jdbcTemplate, transactionTemplate);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource, PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
