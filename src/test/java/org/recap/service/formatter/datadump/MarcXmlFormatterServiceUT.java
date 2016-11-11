package org.recap.service.formatter.datadump;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.model.jpa.*;
import org.recap.repository.BibliographicDetailsRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by premkb on 2/10/16.
 */
public class MarcXmlFormatterServiceUT extends BaseTestCase {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(MarcXmlFormatterServiceUT.class);

    @Autowired
    private MarcXmlFormatterService marcXmlFormatterService;

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private ProducerTemplate producer;

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    private String bibContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<collection>\n" +
            "    <record>\n" +
            "        <leader>00800cas a2200277 i 4500</leader>\n" +
            "        <controlfield tag=\"001\">10</controlfield>\n" +
            "        <controlfield tag=\"003\">NNC</controlfield>\n" +
            "        <controlfield tag=\"005\">20100215174244.0</controlfield>\n" +
            "        <controlfield tag=\"008\">810702c19649999ilufr p       0   a0engxd</controlfield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "            <subfield code=\"a\">(OCoLC)502399218</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "            <subfield code=\"a\">(OCoLC)ocn502399218</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "            <subfield code=\"a\">(CStRLIN)NYCG022-S</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "            <subfield code=\"9\">AAA0010CU</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"035\">\n" +
            "            <subfield code=\"a\">(NNC)10</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"040\">\n" +
            "            <subfield code=\"a\">NNC</subfield>\n" +
            "            <subfield code=\"c\">NNC</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"090\">\n" +
            "            <subfield code=\"a\">TA434</subfield>\n" +
            "            <subfield code=\"b\">.S15</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\"0\" ind2=\"0\" tag=\"245\">\n" +
            "            <subfield code=\"a\">SOâ\u0082\u0083 abstracts &amp; newsletter.</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\"3\" ind2=\"3\" tag=\"246\">\n" +
            "            <subfield code=\"a\">SO three abstracts &amp; newsletter</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"260\">\n" +
            "            <subfield code=\"a\">[Chicago] :</subfield>\n" +
            "            <subfield code=\"b\">United States Gypsum,</subfield>\n" +
            "            <subfield code=\"c\">[1964?]-&lt;1979&gt;</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"300\">\n" +
            "            <subfield code=\"a\">v. :</subfield>\n" +
            "            <subfield code=\"b\">ill. ;</subfield>\n" +
            "            <subfield code=\"c\">28 cm.</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\"0\" ind2=\" \" tag=\"362\">\n" +
            "            <subfield code=\"a\">Vol. 1, no. 1-</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\" \" tag=\"500\">\n" +
            "            <subfield code=\"a\">Editor: W.C. Hansen, 1964-1979.</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\"0\" tag=\"650\">\n" +
            "            <subfield code=\"a\">Cement</subfield>\n" +
            "            <subfield code=\"v\">Periodicals.</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\" \" ind2=\"0\" tag=\"650\">\n" +
            "            <subfield code=\"a\">Gypsum</subfield>\n" +
            "            <subfield code=\"v\">Periodicals.</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\"1\" ind2=\" \" tag=\"700\">\n" +
            "            <subfield code=\"a\">Hansen, W. C.</subfield>\n" +
            "            <subfield code=\"q\">(Waldemar Conrad),</subfield>\n" +
            "            <subfield code=\"d\">1896-</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\"2\" ind2=\" \" tag=\"710\">\n" +
            "            <subfield code=\"a\">United States Gypsum Co.</subfield>\n" +
            "        </datafield>\n" +
            "    </record>\n" +
            "</collection>\n";

    private String holdingContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<collection>\n" +
            "    <record>\n" +
            "        <datafield ind1=\"0\" ind2=\"1\" tag=\"852\">\n" +
            "            <subfield code=\"b\">off,che</subfield>\n" +
            "            <subfield code=\"h\">QD79.C454 H533</subfield>\n" +
            "        </datafield>\n" +
            "        <datafield ind1=\"0\" ind2=\"0\" tag=\"866\">\n" +
            "            <subfield code=\"a\">v.1-v.5</subfield>\n" +
            "        </datafield>\n" +
            "    </record>\n" +
            "</collection>\n";

    @Test
    public void getMarcRecords() throws IOException, URISyntaxException {
        BibliographicEntity bibliographicEntity = getBibliographicEntity();
        Map<String, Object> successAndFailureFormattedList = (Map<String, Object>) marcXmlFormatterService.getFormattedOutput(Arrays.asList(bibliographicEntity));
        String marcXmlString = (String) successAndFailureFormattedList.get(ReCAPConstants.DATADUMP_FORMATTEDSTRING);
        System.out.println(marcXmlString);
        List<Record> recordList = readMarcXml(marcXmlString);
        assertNotNull(recordList);
        assertEquals("SCSB-100", recordList.get(0).getControlFields().get(0).getData());
    }

    private BibliographicEntity getBibliographicEntity() throws URISyntaxException, IOException {
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setBibliographicId(100);
        bibliographicEntity.setContent(bibContent.getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setCreatedBy("tst");
        bibliographicEntity.setLastUpdatedBy("tst");
        bibliographicEntity.setOwningInstitutionBibId("1");
        bibliographicEntity.setOwningInstitutionId(3);
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionId(1);
        institutionEntity.setInstitutionCode("NYPL");
        institutionEntity.setInstitutionName("New York Public Library");
        bibliographicEntity.setInstitutionEntity(institutionEntity);

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setHoldingsId(345);
        holdingsEntity.setContent(holdingContent.getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("tst");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("tst");
        holdingsEntity.setOwningInstitutionId(3);
        holdingsEntity.setOwningInstitutionHoldingsId("54323");
        holdingsEntity.setInstitutionEntity(institutionEntity);

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setCallNumberType("0");
        itemEntity.setCallNumber("callNum");
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("tst");
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setLastUpdatedBy("tst");
        itemEntity.setBarcode("1231");
        itemEntity.setOwningInstitutionItemId(".i1231");
        itemEntity.setOwningInstitutionId(3);
        itemEntity.setCollectionGroupId(1);
        CollectionGroupEntity collectionGroupEntity = new CollectionGroupEntity();
        collectionGroupEntity.setCollectionGroupCode("Shared");
        itemEntity.setCollectionGroupEntity(collectionGroupEntity);
        itemEntity.setCustomerCode("PA");
        itemEntity.setCopyNumber(1);
        itemEntity.setItemAvailabilityStatusId(1);
        ItemStatusEntity itemStatusEntity = new ItemStatusEntity();
        itemStatusEntity.setStatusCode("Available");
        itemEntity.setItemStatusEntity(itemStatusEntity);
        itemEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        holdingsEntity.setItemEntities(Arrays.asList(itemEntity));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));
        return bibliographicEntity;
    }

    private List<Record> readMarcXml(String marcXmlString) {
        List<Record> recordList = new ArrayList<>();
        InputStream in = new ByteArrayInputStream(marcXmlString.getBytes());
        MarcReader reader = new MarcXmlReader(in);
        while (reader.hasNext()) {
            Record record = reader.next();
            recordList.add(record);
            logger.info(record.toString());
        }
        return recordList;
    }

    @Test
    public void generatedFormattedString() throws Exception {
        BibliographicEntity bibliographicEntity = bibliographicDetailsRepository.findOne(new BibliographicPK(1, "100"));

        ArrayList<Record> recordList = new ArrayList<>();
        marcXmlFormatterService.prepareBibEntity(new ArrayList<>(), new ArrayList<>(), recordList, bibliographicEntity);
        Record record = recordList.get(0);
        assertNotNull(record);

        OutputStream out = new ByteArrayOutputStream();
        MarcWriter writer = new MarcXmlWriter(out, "UTF-8", true);
        writeMarcXml(recordList, writer);

        BibliographicEntity bibliographicEntity1 = bibliographicDetailsRepository.findOne(new BibliographicPK(1, "10002"));

        ArrayList<Record> recordList1 = new ArrayList<>();
        marcXmlFormatterService.prepareBibEntity(new ArrayList<>(), new ArrayList<>(), recordList1, bibliographicEntity1);
        Record record1 = recordList.get(0);
        assertNotNull(record1);

        writeMarcXml(recordList1, writer);

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("seda:testStreamOut")
                        .to("stream:file?fileName=" + dumpDirectoryPath + File.separator + "test.xml");
            }
        });

        producer.sendBody("seda:testStreamOut", out.toString());

//        writer.close();
    }

    private void writeMarcXml(ArrayList<Record> recordList, MarcWriter writer) {
        try {
            recordList.forEach(writer::write);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}