package org.recap.model.jpa;

import org.recap.repository.BibliographicDetailsRepository;

import java.util.concurrent.Callable;

/**
 * Created by pvsubrah on 6/24/16.
 */
public class BibRepositoryCallable implements Callable {
    //TODO: Delete the class as not being used.

    private BibliographicEntity bibliographicEntity;
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    public BibRepositoryCallable(BibliographicEntity bibliographicEntity, BibliographicDetailsRepository bibliographicDetailsRepository) {
        this.bibliographicEntity = bibliographicEntity;
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
    }

    @Override
    public Object call() throws Exception {
        BibliographicEntity savedEntity = bibliographicDetailsRepository.save(bibliographicEntity);
        return savedEntity;
    }
}
