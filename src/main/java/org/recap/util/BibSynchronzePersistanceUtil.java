package org.recap.util;

import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by SheikS on 6/24/2016.
 */
public class BibSynchronzePersistanceUtil {
    private Logger logger = LoggerFactory.getLogger(BibSynchronzePersistanceUtil.class);
    private static BibSynchronzePersistanceUtil bibSynchronzePersistanceUtil;
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    public static BibSynchronzePersistanceUtil getInstance() {
        if(null == bibSynchronzePersistanceUtil) {
            bibSynchronzePersistanceUtil = new BibSynchronzePersistanceUtil();
        }
        return bibSynchronzePersistanceUtil;
    }

    private BibSynchronzePersistanceUtil() {
    }

    public synchronized void saveBibRecords(List<BibliographicEntity> bibliographicEntities) {
        long startTime = System.currentTimeMillis();
        try {
            getBibliographicDetailsRepository().save(bibliographicEntities);
        } catch (Exception e) {
            logger.info("Exception " + e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        logger.info("Time taken to persist " + bibliographicEntities.size() + " bibliographic entities is: " + (endTime - startTime) / 1000 + " seconds");
    }

    public BibliographicDetailsRepository getBibliographicDetailsRepository() {
        return bibliographicDetailsRepository;
    }

    public void setBibliographicDetailsRepository(BibliographicDetailsRepository bibliographicDetailsRepository) {
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
    }
}
