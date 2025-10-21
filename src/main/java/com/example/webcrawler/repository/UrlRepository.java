package com.example.webcrawler.repository;

import com.example.webcrawler.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByUrl(String url);


    List<Url> findByStatus(Url.UrlStatus status);

}