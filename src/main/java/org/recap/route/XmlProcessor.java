package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.recap.model.jpa.XmlRecordEntity;
import org.recap.repository.XmlRecordRepository;

import java.util.UUID;

/**
 * Created by peris on 7/17/16.
 */
public class XmlProcessor implements Processor {
    private final XmlRecordRepository xmlRecordRepository;

    public XmlProcessor(XmlRecordRepository xmlRecordRepository) {
        this.xmlRecordRepository = xmlRecordRepository;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String xmlRecord = (String) exchange.getIn().getBody();
        XmlRecordEntity xmlRecordEntity = new XmlRecordEntity();
        xmlRecordEntity.setXml(xmlRecord);
        String camelFileName = (String) exchange.getIn().getHeader("CamelFileName");
        xmlRecordEntity.setXmlFileName(camelFileName);
        xmlRecordRepository.save(xmlRecordEntity);
    }
}
