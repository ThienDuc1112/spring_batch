package com.example.migration.repository;

import com.example.migration.model.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetadataRepository extends JpaRepository<Metadata, Long> {
    List<Metadata> findByStatus(String status);
    List<Metadata> findByFileName(String fileName);
}