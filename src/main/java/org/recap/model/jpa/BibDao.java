package org.recap.model.jpa;

import java.util.List;

/**
 * Created by peris on 7/18/16.
 */
public interface BibDao {
    public List<BibliographicEntity> saveBatch(List<BibliographicEntity> bibliographicEntities);
}
