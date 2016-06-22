package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.recap.model.BibliographicEntityGenerator;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class RecordProcessor implements Processor {
    private static Logger logger = LoggerFactory.getLogger(RecordProcessor.class);
    private JAXBHandler jaxbHandler;
    private BibliographicEntityGenerator bibliographicEntityGenerator;
    private BibliographicDetailsRepository bibliographicDetailsRepository;


    public RecordProcessor(BibliographicDetailsRepository bibliographicDetailsRepository) {
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        if (exchange.getIn().getBody() instanceof List) {

            List<BibliographicEntity> bibliographicEntities = new ArrayList<>();

            for (String content : (List<String>) exchange.getIn().getBody()) {
                BibRecord bibRecord = (BibRecord) getJaxbHandler().unmarshal(content, BibRecord.class);
                bibliographicEntities.add(getBibliographicEntityGenerator().generateBibliographicEntity(bibRecord));
            }

            bibliographicDetailsRepository.save(bibliographicEntities);
        }
    }

    public BibliographicEntityGenerator getBibliographicEntityGenerator() {
        if (null == bibliographicEntityGenerator) {
            bibliographicEntityGenerator = new BibliographicEntityGenerator();
        }
        return bibliographicEntityGenerator;
    }

    public JAXBHandler getJaxbHandler() {
        if (null == jaxbHandler) {
            jaxbHandler = JAXBHandler.getInstance();
        }
        return jaxbHandler;
    }
}
