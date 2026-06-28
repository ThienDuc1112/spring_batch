package com.example.migration.service;

import com.example.migration.model.Metadata;
import com.example.migration.repository.MetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationService {

    private final MetadataRepository metadataRepository;

    @Transactional
    public Metadata saveMetadata(Metadata metadata) {
        return metadataRepository.save(metadata);
    }

    public Metadata getMetadataById(Long id) {
        return metadataRepository.findById(id).orElse(null);
    }

    public List<Metadata> getAllMetadata() {
        return metadataRepository.findAll();
    }

    public List<Metadata> getMetadataByStatus(String status) {
        return metadataRepository.findByStatus(status);
    }

    @Transactional
    public void updateStatus(Long id, String status, String errorMessage) {
        Metadata metadata = getMetadataById(id);
        if (metadata != null) {
            metadata.setStatus(status);
            metadata.setErrorMessage(errorMessage);
            metadataRepository.save(metadata);
        }
    }
}