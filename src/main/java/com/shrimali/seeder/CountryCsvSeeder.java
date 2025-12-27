package com.shrimali.seeder;

import com.shrimali.model.geo.Country;
import com.shrimali.repositories.geo.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStreamReader;
import java.io.Reader;

@Slf4j
@Component
@Profile({"seed", "geo"})
@RequiredArgsConstructor
@Transactional
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class CountryCsvSeeder implements ApplicationRunner {
    private final CountryRepository countryRepo;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource =
                new ClassPathResource("geo/countries.csv");

        try (
                Reader reader = new InputStreamReader(resource.getInputStream());
                CSVParser csvParser = CSVFormat.DEFAULT.builder()
                        .setHeader()                      // replaces withFirstRecordAsHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreSurroundingSpaces(true) // replaces withIgnoreSurroundingSpaces()
                        .setTrim(true).get()
                        .parse(reader)
        ) {
            for (CSVRecord record : csvParser) {

                String code = record.get("iso2"); // or "ISO2"
                String name = record.get("name");

                if (code == null || code.isBlank()) continue;

                if (!countryRepo.existsByCode(code)) {
                    Country country = new Country();
                    country.setCode(code);
                    country.setName(name);
                    country.setActive(true);
                    countryRepo.save(country);
                }
            }
        }

        log.info("✅ Countries seeded safely");
    }
}
