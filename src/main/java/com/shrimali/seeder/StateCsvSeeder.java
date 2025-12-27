package com.shrimali.seeder;

import com.shrimali.model.geo.Country;
import com.shrimali.model.geo.State;
import com.shrimali.repositories.geo.CountryRepository;
import com.shrimali.repositories.geo.StateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Profile({"seed", "geo"})
@RequiredArgsConstructor
@Slf4j
@Order(value = 2)
public class StateCsvSeeder implements ApplicationRunner {
    private static final int BATCH_SIZE = 500;

    private final CountryRepository countryRepo;
    private final StateRepository stateRepo;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("🚀 Starting high-speed state seeding...");
        ClassPathResource resource = new ClassPathResource("geo/states.csv");

        Map<String, Country> countryMap = countryRepo.findAll().stream()
                .collect(Collectors.toMap(Country::getCode, c -> c));

        Set<String> existingStateKeys = new HashSet<>(stateRepo.findAllStateKeys());
        log.info("✅ Cached {} existing states", existingStateKeys.size());

        try (
                Reader reader = new InputStreamReader(resource.getInputStream());
                CSVParser csvParser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreSurroundingSpaces(true)
                        .setTrim(true).get()
                        .parse(reader)
        ) {
            List<State> batch = new ArrayList<>(BATCH_SIZE);
            int inserted = 0;
            int skipped = 0;
            int processed = 0;

            for (CSVRecord record : csvParser) {
                processed++;

                String countryCode = record.get("country_code");
                String stateCode = record.get("iso2");
                String stateName = record.get("name");

                // ✅ Skip if state code is NULL, EMPTY, or BLANK
                if (!StringUtils.hasText(countryCode) || !StringUtils.hasText(stateCode)
                        || !StringUtils.hasText(stateName) || "NULL".equalsIgnoreCase(stateCode)) {
                    skipped++;
                    continue;
                }

                Country country = countryMap.get(countryCode);
                if (country == null) {
                    skipped++;
                    continue;
                }

                String stateKey = country.getId() + ":" + stateCode.trim();
                if (!existingStateKeys.add(stateKey)) {
                    skipped++;
                    continue;
                }

                State state = new State();
                state.setCountry(country);
                state.setCode(stateCode);
                state.setName(stateName);
                state.setActive(true);

                batch.add(state);
                inserted++;

                if (batch.size() == BATCH_SIZE) {
                    executeJdbcBatch(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                executeJdbcBatch(batch);
            }

            log.info("✅ State seeding complete | processed={} inserted={} skipped={}", processed, inserted, skipped);
        }
    }

    private void executeJdbcBatch(List<State> batch) {
        // Use ON CONFLICT to prevent crashes from encoding issues or existing data
        String sql = """
                INSERT INTO states (country_id, code, name, active, created_at, updated_at)
                VALUES (?, ?, ?, ?, NOW(), NOW())
                ON CONFLICT (country_id, code) DO NOTHING
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                State state = batch.get(i);
                ps.setLong(1, state.getCountry().getId());
                ps.setString(2, state.getCode());
                ps.setString(3, state.getName());
                ps.setBoolean(4, state.isActive());
            }

            @Override
            public int getBatchSize() {
                return batch.size();
            }
        });
    }
}
