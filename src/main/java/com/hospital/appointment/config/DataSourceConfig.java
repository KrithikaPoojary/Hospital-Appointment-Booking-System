package com.hospital.appointment.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Intelligent DataSource configuration that automatically parses and adapts
 * standard cloud database connection URLs (e.g., Render, Heroku, Neon, Supabase)
 * which use "postgresql://user:pass@host/db" into the exact JDBC-compliant format
 * "jdbc:postgresql://host:port/db".
 *
 * Runs on all non-test profiles, leaving H2 in-memory test configuration intact.
 */
@Configuration
@Profile("!test")
@Slf4j
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String rawUrl;

    @Value("${spring.datasource.username:}")
    private String defaultUsername;

    @Value("${spring.datasource.password:}")
    private String defaultPassword;

    @Bean
    public DataSource dataSource() {
        String jdbcUrl = rawUrl;
        String username = defaultUsername;
        String password = defaultPassword;

        try {
            // Strip any leading jdbc: if present to parse cleanly
            String cleanUrl = rawUrl.startsWith("jdbc:") ? rawUrl.substring(5) : rawUrl;

            if (cleanUrl.startsWith("postgres://") || cleanUrl.startsWith("postgresql://")) {
                URI uri = new URI(cleanUrl);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();

                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;

                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    username = userInfo[0];
                    if (userInfo.length > 1) {
                        password = userInfo[1];
                    }
                }
                log.info("Adapted cloud PostgreSQL connection to JDBC format: jdbc:postgresql://{}:{}{}", host, port, path);
            } else if (!rawUrl.startsWith("jdbc:")) {
                jdbcUrl = "jdbc:" + rawUrl;
            }
        } catch (Exception e) {
            log.warn("Could not parse datasource URL as URI, using original: {}", e.getMessage());
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(config);
    }
}
