package org.recap.route;

import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by pvsubrah on 6/26/16.
 */

@Component
public class JMSMessageProcessor {

    Logger logger = LoggerFactory.getLogger(JMSMessageProcessor.class);

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    public void processMessage(List<BibliographicEntity> bibliographicEntityList) {
        long startTime = System.currentTimeMillis();
        try {
            bibliographicDetailsRepository.save(bibliographicEntityList);
        } catch (Exception e) {
            for (BibliographicEntity bibliographicEntity : bibliographicEntityList) {
                try {
                    bibliographicDetailsRepository.save(bibliographicEntity);
                } catch (Exception ex) {
                    logger.error("Exception " + ex);
                }
            }
        }
        long endTime = System.currentTimeMillis();
        logger.info("Time taken to save: " + bibliographicEntityList.size() + " bib and related data is: " + (endTime - startTime) / 1000 + " seconds.");
    }
}
