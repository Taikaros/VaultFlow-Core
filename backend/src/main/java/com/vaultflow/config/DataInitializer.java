package com.vaultflow.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Component
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DataInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        String url = dataSource.getConnection().getMetaData().getURL();
        if (!url.contains("sqlite")) {
            return;
        }

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM companies", Long.class);
        if (count != null && count == 0) {
            var resource = new ClassPathResource("db/seed.sql");
            String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    jdbcTemplate.execute(trimmed);
                }
            }
            System.out.println("Seed data cargada exitosamente");
        }
    }
}
