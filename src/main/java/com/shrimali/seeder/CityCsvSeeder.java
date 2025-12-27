package com.shrimali.seeder;

import com.shrimali.model.geo.City;
import com.shrimali.model.geo.Country;
import com.shrimali.model.geo.State;
import com.shrimali.repositories.geo.CityRepository;
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
@Order(value = 3)
public class CityCsvSeeder implements ApplicationRunner {
    private static final int BATCH_SIZE = 1000;

    private final CountryRepository countryRepo;
    private final StateRepository stateRepo;
    private final CityRepository cityRepo;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("🚀 Starting high-speed city seeding...");
        ClassPathResource resource = new ClassPathResource("geo/cities.csv");

        Map<String, Map<String, State>> stateCache = stateRepo.findAllWithCountry().stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCountry().getCode(),
                        Collectors.toMap(State::getCode, s -> s, (existing, replacement) -> existing)
                ));

        log.info("🔍 Loading existing cities into memory cache...");
        Set<String> existingCityKeys = new HashSet<>(cityRepo.findAllCityKeys());
        log.info("✅ Cached {} existing cities", existingCityKeys.size());

        try (
                Reader reader = new InputStreamReader(resource.getInputStream());
                CSVParser csvParser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreSurroundingSpaces(true)
                        .setTrim(true).get()
                        .parse(reader)
        ) {
            List<City> batch = new ArrayList<>(BATCH_SIZE);
            int inserted = 0;
            int skipped = 0;
            int processed = 0;

            Set<String> seenInRun = new HashSet<>();
            for (CSVRecord record : csvParser) {
                processed++;

                String countryCode = record.get("country_code");
                String stateCode = record.get("state_code");
                String cityName = record.get("name");

                if (!StringUtils.hasText(countryCode) || !StringUtils.hasText(stateCode) || !StringUtils.hasText(cityName)) {
                    skipped++;
                    continue;
                }

                State state = stateCache.getOrDefault(countryCode, Map.of()).get(stateCode);
                if (state == null) {
                    skipped++;
                    continue;
                }

                // Create a unique key for the City-State combination
                String cleanName = cityName.trim(); // Remove invisible spaces
                String cityKey = state.getId() + ":" + cleanName.toLowerCase();

                // CHECK: If it exists in DB cache OR if we've already seen it in this CSV (using the same Set)
                if (!existingCityKeys.add(cityKey)) {
                    skipped++;
                    continue;
                }

                City city = City.builder()
                        .state(state)
                        .name(cityName)
                        .latitude(parseCoordinate(record, "latitude"))
                        .longitude(parseCoordinate(record, "longitude"))
                        .active(true)
                        .build();

                batch.add(city);
                inserted++;

                if (batch.size() == BATCH_SIZE) {
                    executeJdbcBatch(batch);
                    batch.clear();
                }

                if (processed % 20_000 == 0) {
                    log.info("📍 Progress: Processed={}, Inserted={}, Skipped={}", processed, inserted, skipped);
                }
            }

            if (!batch.isEmpty()) {
                executeJdbcBatch(batch);
            }

            log.info("✅ Seeding complete: {} inserted, {} skipped.", inserted, skipped);

        }
    }

    private void executeJdbcBatch(List<City> batch) {
        // Adding ON CONFLICT (state_id, name) DO NOTHING
        String sql = """
                INSERT INTO cities (name, state_id, latitude, longitude, active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, NOW(), NOW())
                ON CONFLICT (state_id, name) DO NOTHING
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                City city = batch.get(i);
                ps.setString(1, city.getName());
                ps.setLong(2, city.getState().getId());
                ps.setObject(3, city.getLatitude());
                ps.setObject(4, city.getLongitude());
                ps.setBoolean(5, city.isActive());
            }

            @Override
            public int getBatchSize() {
                return batch.size();
            }
        });
    }

    private Double parseCoordinate(CSVRecord record, String column) {
        try {
            String val = record.isMapped(column) ? record.get(column) : null;
            return (StringUtils.hasText(val) && !"NULL".equalsIgnoreCase(val)) ? Double.valueOf(val) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
