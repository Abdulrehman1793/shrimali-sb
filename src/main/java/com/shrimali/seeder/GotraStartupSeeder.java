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
@RequiredArgsConstructor
@Transactional
public class GotraStartupSeeder implements ApplicationRunner {

    private final GotraRepository gotraRepository;

    @Override
    public void run(@NonNull ApplicationArguments args) {

        // ============================
// Core Saptarishi Gotras
// ============================
        Gotra kashyap = seedCore(
                "कश्यप",
                "Kashyap",
                "Descendants of Maharishi Kashyap, one of the Saptarishis.", 1
        );

        Gotra bharadwaj = seedCore(
                "भारद्वाज",
                "Bharadwaj",
                "Lineage of Maharishi Bharadwaj, associated with Vedic scholarship.", 2
        );

        Gotra gautam = seedCore(
                "गौतम",
                "Gautam",
                "Descendants of Maharishi Gautama, founder of Nyaya philosophy.", 3
        );

        Gotra kaushik = seedCore(
                "कौशिक",
                "Kaushik",
                "Descendants of Maharishi Vishwamitra (Kaushika lineage).", 4
        );

        Gotra atri = seedCore(
                "अत्रि",
                "Atri",
                "Descendants of Maharishi Atri, one of the Saptarishis.", 5
        );

        // Internal core (needed for correct lineage)
        Gotra vashistha = seedCore(
                "वशिष्ठ",
                "Vashistha",
                "Lineage of Maharishi Vashistha, preserved for Parashar gotra mapping.", 6
        );
        Gotra others = seedCore("अन्य", "Others", "Community members belonging to other recognized gotras or lineages.", 17);

        // ============================
        // Sub-Gotras (from updated array)
        // ============================

        seedSub(
                "संकस",
                "Sankas",
                "Ancient gotra associated with the Shrimali Brahmin community.",
                kaushik, 7
        );

        seedSub(
                "वत्स",
                "Vatsa",
                "Lineage associated with Maharishi Vatsa.",
                bharadwaj, 8
        );

        seedSub(
                "पराशर",
                "Parashar",
                "Lineage of Maharishi Parashar, father of Ved Vyasa.",
                vashistha, 9
        );

        seedSub(
                "हरितस",
                "Haritas",
                "Descendants of Maharishi Harita, linked to ancient Dharmashastras.",
                kashyap, 10
        );

        seedSub(
                "उपमन्यु",
                "Upamanyu",
                "Lineage of Maharishi Upamanyu, known for devotion and penance.",
                gautam, 11
        );

        seedSub(
                "कपिंजल",
                "Kapingal",
                "Traditional Kapingal gotra referenced in Shrimali lineage.",
                kashyap, 12
        );

        seedSub(
                "चंद्र",
                "Chandra",
                "Traditional Chandra gotra associated with discipline and virtue.",
                atri, 13
        );

        seedSub(
                "लौदावन",
                "Laudavan",
                "Regional Shrimali gotra associated with Chamunda tradition.",
                kashyap, 14
        );

        seedSub(
                "मौद्गल",
                "Maudgal",
                "Lineage of Maharishi Mudgala, referenced in Vedic texts.",
                bharadwaj, 15
        );

        seedSub(
                "शांडिल्य",
                "Shandilya",
                "Lineage of Maharishi Shandilya, associated with Smriti traditions.",
                atri, 16
        );
    }

    /* ============================
       Seeder Helpers
       ============================ */

    private Gotra seedCore(String nameHi, String name, String description, int order) {
        Gotra gotra = gotraRepository.findByNameIgnoreCase(name)
                .orElseGet(Gotra::new);

        boolean isNew = gotra.getId() == null;

        gotra.setName(name);
        gotra.setCore(true);
        gotra.setDescription(description);

        // ✅ Set displayOrder only if missing
        if (gotra.getDisplayOrder() == null) {
            gotra.setDisplayOrder(order);
            if (!isNew) {
                log.info("Updated missing displayOrder for core gotra: {}", name);
            }
        }

        // ✅ Update only if empty
        if (gotra.getNameHi() == null || gotra.getNameHi().isBlank()) {
            gotra.setNameHi(nameHi);
            if (!isNew) {
                log.info("Updated missing nameHi for core gotra: {}", name);
            }
        }

        if (isNew) {
            log.info("Seeding core gotra: {}", name);
        }

        return gotraRepository.save(gotra);
    }

    private void seedSub(String nameHi, String name, String description, Gotra parentGotra, int order) {
        Gotra gotra = gotraRepository.findByNameIgnoreCase(name)
                .orElseGet(Gotra::new);

        boolean isNew = gotra.getId() == null;

        gotra.setName(name);
        gotra.setCore(false);
        gotra.setDescription(description);
        gotra.setParentGotra(parentGotra);

        // ✅ Set displayOrder only if missing
        if (gotra.getDisplayOrder() == null) {
            gotra.setDisplayOrder(order);
            if (!isNew) {
                log.info("Updated missing displayOrder for sub gotra: {}", name);
            }
        }

        // ✅ Update only if empty
        if (gotra.getNameHi() == null || gotra.getNameHi().isBlank()) {
            gotra.setNameHi(nameHi);
            if (!isNew) {
                log.info("Updated missing nameHi for sub gotra: {}", name);
            }
        }

        if (isNew) {
            log.info("Seeding sub gotra: {}", name);
        }

        gotraRepository.save(gotra);
    }
}

