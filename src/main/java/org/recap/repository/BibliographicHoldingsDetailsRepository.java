package org.recap.repository;

import org.recap.model.jpa.BibliographicHoldingsEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chenchulakshmig on 6/13/16.
 */
public interface BibliographicHoldingsDetailsRepository extends CrudRepository<BibliographicHoldingsEntity, Integer> {
    public List<BibliographicHoldingsEntity> findAllByHoldingsId(Integer holdingsId);
    List<BibliographicHoldingsEntity> findAllByBibliographicId(Integer bibliographicId);
}
