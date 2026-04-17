package com.paybridge.recon.run;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconRunJpaRepository extends JpaRepository<ReconRunJpaEntity, UUID> {

    List<ReconRunJpaEntity> findTop10ByOrderByStartedAtDesc();
}
