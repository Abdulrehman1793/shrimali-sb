package com.shrimali.repositories;

import com.shrimali.model.member.ClaimActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimActionLogRepository extends JpaRepository<ClaimActionLog, Long> {
}
