package com.janvi.urlshortener.url.repository;

import com.janvi.urlshortener.url.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    boolean existsByShortCode(String shortCode);

    Optional<Url> findByShortCode(String shortCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Url u
            set u.clickCount = coalesce(u.clickCount, 0) + :delta
            where u.shortCode = :shortCode
            """)
    int incrementClickCount(
            @Param("shortCode") String shortCode,
            @Param("delta") long delta
    );

    Optional<Url> findByShortCodeAndUserEmail(
            String shortCode,
            String email
    );
}