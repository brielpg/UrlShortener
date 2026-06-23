package br.com.url_shortener.infrastructure.config;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HashidsConfig {

    @Value("${app.hashids.salt}")
    private String salt;

    @Bean
    public Hashids hashids(){
        return new Hashids(salt, 5);
    }
}
