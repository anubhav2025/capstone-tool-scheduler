package com.capstone.toolScheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capstone.toolScheduler.model.Credential;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    // Finds by matching owner + repository
    Credential findByOwnerAndRepository(String owner, String repository);
}

