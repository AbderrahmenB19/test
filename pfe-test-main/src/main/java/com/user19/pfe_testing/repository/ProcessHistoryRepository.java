package com.user19.pfe_testing.repository;

import com.user19.pfe_testing.model.ProcessHistory;
import com.user19.pfe_testing.model.ProcessInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessHistoryRepository extends JpaRepository<ProcessHistory,String> {
    List<ProcessHistory> findByProcessInstanceAndAction(ProcessInstance processInstance, String action);


}
