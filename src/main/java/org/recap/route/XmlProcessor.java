package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.recap.model.jpa.XmlRecordEntity;
import org.recap.repository.XmlRecordRepository;

import java.util.Date;
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
        String owningInstitutionId = StringUtils.substringBetween(xmlRecord, "<owningInstitutionId>", "</owningInstitutionId>");
        xmlRecordEntity.setOwningInst(owningInstitutionId);
        String owningInstitutionBibId = StringUtils.substringBetween(xmlRecord, "<owningInstitutionBibId>", "</owningInstitutionBibId>");
        if(StringUtils.isBlank(owningInstitutionBibId)){
            owningInstitutionBibId = StringUtils.substringBetween(xmlRecord, "<controlfield tag=\"001\">","</controlfield>");
        }
        if (StringUtils.isBlank(owningInstitutionBibId)){
            owningInstitutionBibId = StringUtils.substringBetween(xmlRecord, "<controlfield tag='001'>","</controlfield>");
        }
        xmlRecordEntity.setOwningInstBibId(owningInstitutionBibId);
        Date date = new Date();
        xmlRecordEntity.setDataLoaded(date);
        xmlRecordRepository.save(xmlRecordEntity);
    }
}
