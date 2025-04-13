package com.echo_english.repository;

import com.echo_english.entity.GramSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GramSectionRepository extends JpaRepository<GramSection, Long> {
}