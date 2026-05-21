package com.howprom.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.howprom.common.entity.Requirement;

public interface RequirementRepository extends JpaRepository<Requirement, Long> {
}