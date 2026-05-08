package com.group32.cpt202.lpp_project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceArchiveRecordRepository extends JpaRepository<ResourceArchiveRecord, Long> {
    List<ResourceArchiveRecord> findAllByOrderByArchiveTimeDesc();

    void deleteByResourceId(Long resourceId);
}
