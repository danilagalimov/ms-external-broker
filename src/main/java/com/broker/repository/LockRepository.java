package com.broker.repository;

import com.broker.data.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LockRepository extends JpaRepository<Lock, UUID> {

    long removeById(UUID id);
}
