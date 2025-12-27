package com.shrimali.seeder;

import com.shrimali.model.Gotra;
import com.shrimali.repositories.GotraRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
@Transactional
public class GotraStartupSeeder implements ApplicationRunner {

    private final GotraRepository gotraRepository;

    @Override
    public void run(@NonNull ApplicationArguments args) {

        // ============================
        // Core Saptarishi Gotras
        // ============================
        Gotra kashyap   = seedCore("Kashyap", "Descendants of Maharishi Kashyap, one of the Saptarishis.");
        Gotra bharadwaj = seedCore("Bharadwaj", "Lineage of Maharishi Bharadwaj, associated with Vedic scholarship.");
        Gotra vashistha = seedCore("Vashistha", "Lineage of Maharishi Vashistha, royal priest of the Ikshvaku dynasty.");
        Gotra gautam    = seedCore("Gautam", "Descendants of Maharishi Gautama, founder of Nyaya philosophy.");
        Gotra atri      = seedCore("Atri", "Descendants of Maharishi Atri, one of the Saptarishis.");
        Gotra jamadagni = seedCore("Jamadagni", "Lineage of Maharishi Jamadagni, father of Parashurama.");
        Gotra kaushik   = seedCore("Kaushik", "Descendants of Maharishi Vishwamitra (Kaushika lineage).");

        // ============================
        // Extended / Sub-Gotras
        // ============================
        seedSub("Parashar", "Lineage of Maharishi Parashar, father of Ved Vyasa.", vashistha);
        seedSub("Agastya", "Descendants of Maharishi Agastya, revered in North and South India.", atri);
        seedSub("Haritas", "Descendants of Maharishi Harita, linked to ancient Dharmashastras.", kashyap);
        seedSub("Vatsa", "Lineage associated with Maharishi Vatsa.", bharadwaj);
        seedSub("Upamanyu", "Lineage of Maharishi Upamanyu, known for devotion and penance.", gautam);
        seedSub("Sankas", "Ancient gotra associated with the Shrimali Brahmin community.", kaushik);
        seedSub("Shandilya", "Lineage of Maharishi Shandilya, associated with Smriti traditions.", atri);
        seedSub("Kapila", "Lineage of Maharishi Kapila, founder of Sankhya philosophy.", kashyap);
        seedSub("Mandavya", "Lineage of Maharishi Mandavya, associated with penance and justice.", vashistha);
        seedSub("Lohit", "Ancient lineage referenced in regional Brahmin traditions.", kashyap);
    }

    /* ============================
       Seeder Helpers
       ============================ */

    private Gotra seedCore(String name, String description) {
        return gotraRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Gotra gotra = new Gotra();
                    gotra.setName(name);
                    gotra.setDescription(description);
                    gotra.setCore(true);
                    return gotraRepository.save(gotra);
                });
    }

    private void seedSub(String name, String description, Gotra parentGotra) {
        gotraRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Gotra gotra = new Gotra();
                    gotra.setName(name);
                    gotra.setDescription(description);
                    gotra.setCore(false);
                    gotra.setParentGotra(parentGotra);
                    return gotraRepository.save(gotra);
                });
    }
}

