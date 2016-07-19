package org.recap.model.jpa;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peris on 7/18/16.
 */

@Repository
public class BibDaoImpl implements BibDao {
    protected EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    public List<BibliographicEntity> saveBatch(List<BibliographicEntity> bibliographicEntities) {
        final List<BibliographicEntity> savedEntities = new ArrayList<>(bibliographicEntities.size());
        int i = 0;
        for (BibliographicEntity bibliographicEntity : bibliographicEntities) {
            BibliographicEntity persistOrMerge = persistOrMerge(bibliographicEntity);
            savedEntities.add(persistOrMerge);
            i++;
            if (i % 500 == 0) {
                // Flush a batch of inserts and release memory.
                entityManager.flush();
                entityManager.clear();
            }

        }

        return savedEntities;
    }

    private BibliographicEntity persistOrMerge(BibliographicEntity bibliographicEntity) {
        BibliographicPK primaryKey = new BibliographicPK();
        primaryKey.setOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionBibId());
        primaryKey.setOwningInstitutionId(bibliographicEntity.getOwningInstitutionId());
        if (entityManager.find(BibliographicEntity.class, primaryKey) == null) {
            entityManager.persist(bibliographicEntity);
            return bibliographicEntity;
        } else {
            BibliographicEntity merge = entityManager.merge(bibliographicEntity);
            return merge;
        }
    }
}
