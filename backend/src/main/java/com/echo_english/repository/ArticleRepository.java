package com.echo_english.repository;
import com.echo_english.entity.WebArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<WebArticle, Long> {
    boolean existsByUrl(String url);
    Optional<WebArticle> findByUrl(String url);
    Page<WebArticle> findBySuitableForLearnersOrderByPublishedDateDesc(boolean suitable, Pageable pageable);
}
