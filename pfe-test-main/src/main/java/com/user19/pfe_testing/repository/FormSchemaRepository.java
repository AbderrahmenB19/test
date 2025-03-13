package com.user19.pfe_testing.repository;

import com.user19.pfe_testing.model.FormSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormSchemaRepository extends JpaRepository<FormSchema,String> {
}
