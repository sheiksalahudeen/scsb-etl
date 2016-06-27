package org.recap.route;

import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by pvsubrah on 6/26/16.
 */

@Component
public class JMSMessageProcessor {

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    public void processMessage(List<BibliographicEntity> bibliographicEntityList){
        bibliographicDetailsRepository.save(bibliographicEntityList);
    }
}
