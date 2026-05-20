package com.howprom.admin.repository;

import com.howprom.admin.entity.Requirement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementRepository extends JpaRepository<Requirement, Long> {
}