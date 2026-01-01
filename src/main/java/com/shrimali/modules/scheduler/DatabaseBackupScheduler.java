package com.shrimali.modules.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DatabaseBackupScheduler {

    @Value("${app.backup.path}")
    private String backupPath;

    @Value("${app.backup.database}")
    private String dbName;

    @Value("${app.backup.username}")
    private String dbUser;

    @Value("${app.backup.password}")
    private String dbPassword;

    @Value("${app.backup.bin-path}")
    private String pgDumpPath;

    //    @Scheduled(cron = "${app.backup.cron}")
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void executeBackup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String fileName = dbName + "_" + timestamp + ".sql";

        String os = System.getProperty("os.name").toLowerCase();
        // On Linux/EC2, we just call "pg_dump". On Windows, we use the full path.
        String command = os.contains("win")
                ? pgDumpPath
                : "pg_dump";

        File directory = new File(backupPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fullPath = backupPath + fileName;

        // Command: pg_dump -U username -d dbname -f output_file
        ProcessBuilder pb = new ProcessBuilder(
                command,
                "-U", dbUser,
                "-Fc",
                "--no-owner",      // Makes restoring to a different user easier
                "--no-privileges", // Prevents permission errors on different environments
                "-d", dbName,
                "-f", fullPath
        );

        // Crucial: Set the password via environment variable to avoid clear-text prompts
        // In production, better to use a .pgpass file on the OS
        pb.environment().put("PGPASSWORD", dbPassword);

        try {
            log.info("Starting backup for {} to {}", dbName, fullPath);
            Process process = pb.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("Backup completed successfully: {}", fileName);
            } else {
                log.error("Backup failed with exit code: {}", exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error during database backup", e);
            Thread.currentThread().interrupt();
        }
    }
}
