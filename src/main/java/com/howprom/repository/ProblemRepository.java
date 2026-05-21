package com.howprom.repository;

import com.howprom.admin.dto.ProblemStatsDTO;
import com.howprom.common.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query("SELECT new com.howprom.admin.dto.ProblemStatsDTO(s.problem.id, COUNT(s.id), AVG(s.score)) " +
               "FROM Submission s GROUP BY s.problem.id")
    List<ProblemStatsDTO> getProblemStatistics();

    List<Problem> findByTitleContaining(String title);

    List<Problem> findByIsPublicTrueOrderByCreatedAtAsc();

    Optional<Problem> findByIdAndIsPublicTrue(Long id);
}