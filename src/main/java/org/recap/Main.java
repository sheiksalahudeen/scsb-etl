package org.recap;

import org.recap.model.BibliographicEntityGenerator;
import org.recap.repository.InstitutionDetailsRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {

    @Bean
    public BibliographicEntityGenerator getBibliographicEntityGenerator(InstitutionDetailsRepository institutionDetailsRepository){
        BibliographicEntityGenerator bibliographicEntityGenerator = new BibliographicEntityGenerator();
        bibliographicEntityGenerator.setInstitutionDetailsRepository(institutionDetailsRepository);
        return bibliographicEntityGenerator;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
