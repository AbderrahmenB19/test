package com.user19.pfe_testing.repository;

import com.user19.pfe_testing.model.ProcessDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessDefinitionRepository extends JpaRepository<ProcessDefinition,String> {

    Optional<ProcessDefinition> findByFormSchemaId(String formSchemaId);


}
