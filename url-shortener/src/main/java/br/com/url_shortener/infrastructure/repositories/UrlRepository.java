package br.com.url_shortener.infrastructure.repositories;

import br.com.url_shortener.domain.models.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<Url, String> {
}
