package com.user19.pfe_testing.repository;

import com.user19.pfe_testing.model.ProcessInstance;
import com.user19.pfe_testing.model.enums.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessInstanceRepository extends JpaRepository<ProcessInstance, String > {
     ProcessInstance findByActorIdAndStatus(String actorId, ProcessStatus processStatus);
    List<ProcessInstance> findByActorId(String actorId);
}
