package com.user19.pfe_testing.repository;

import com.user19.pfe_testing.model.ProcessInstance;
import com.user19.pfe_testing.model.ProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessStepRepository extends JpaRepository<ProcessStep,String> {



    ProcessStep findByName(String currentStepName);
}
