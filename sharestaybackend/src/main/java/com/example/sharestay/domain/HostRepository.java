package com.example.sharestay.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostRepository extends JpaRepository<Host, Long> {
    Optional<Host> findByUser(User user);
}
