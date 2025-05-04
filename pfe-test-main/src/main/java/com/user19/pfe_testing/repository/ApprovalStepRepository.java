package com.user19.pfe_testing.repository;

import com.user19.pfe_testing.model.ApprovalStep;
import com.user19.pfe_testing.model.ProcessDefinition;
import com.user19.pfe_testing.model.ProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    Optional<ApprovalStep> findByNameAndProcessDefinition(String name, ProcessDefinition processDefinition);


}
