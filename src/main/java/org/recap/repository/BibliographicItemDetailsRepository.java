package org.recap.repository;

import org.recap.model.jpa.BibliographicItemEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chenchulakshmig on 23/6/16.
 */
public interface BibliographicItemDetailsRepository extends CrudRepository<BibliographicItemEntity, Integer> {
    public List<BibliographicItemEntity> findAllByItemId(Integer itemId);
    List<BibliographicItemEntity> findAllByBibliographicId(Integer bibliographicId);
}
