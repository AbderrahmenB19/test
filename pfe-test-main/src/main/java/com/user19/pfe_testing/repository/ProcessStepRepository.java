package com.user19.pfe_testing.repository;

import com.user19.pfe_testing.model.ProcessDefinition;
import com.user19.pfe_testing.model.ProcessInstance;
import com.user19.pfe_testing.model.ProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessStepRepository extends JpaRepository<ProcessStep,Long> {



    ProcessStep findByName(String currentStepName);

    List<ProcessStep> findByProcessDefinition(ProcessDefinition processDefinition);

    boolean existsByName(String name);
}
