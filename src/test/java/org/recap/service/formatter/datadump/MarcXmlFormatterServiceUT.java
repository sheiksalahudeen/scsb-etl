package org.recap.service.formatter.datadump;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.stream.StreamEndpoint;
import org.apache.camel.component.stream.StreamProducer;
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

import static org.codehaus.plexus.util.MatchPatterns.from;
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

    private String bibContent = "<collection>\n" +
            "                <record>\n" +
            "                    <controlfield tag=\"001\">NYPG002000036-B</controlfield>\n" +
            "                    <controlfield tag=\"005\">20001116192424.2</controlfield>\n" +
            "                    <controlfield tag=\"008\">850225r19731907nyu b 001 0 ara</controlfield>\n" +
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"010\">\n" +
            "                        <subfield code=\"a\">   77173005  </subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"040\">\n" +
            "                        <subfield code=\"c\">NN</subfield>\n" +
            "                        <subfield code=\"d\">NN</subfield>\n" +
            "                        <subfield code=\"d\">CStRLIN</subfield>\n" +
            "                        <subfield code=\"d\">WaOLN</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"043\">\n" +
            "                        <subfield code=\"a\">ff-----</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\"0\" ind2=\"0\" tag=\"050\">\n" +
            "                        <subfield code=\"a\">DS36.6</subfield>\n" +
            "                        <subfield code=\"b\">.I26 1973</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\"0\" ind2=\"0\" tag=\"082\">\n" +
            "                        <subfield code=\"a\">910.031/767</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\"1\" ind2=\" \" tag=\"100\">\n" +
            "                        <subfield code=\"a\">Ibn Jubayr, MuhÌ£ammad ibn AhÌ£mad,</subfield>\n" +
            "                        <subfield code=\"d\">1145-1217.</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\"1\" ind2=\"0\" tag=\"245\">\n" +
            "                        <subfield code=\"a\">RihÌ£lat</subfield>\n" +
            "                        <subfield code=\"b\">AbÄ« al-Husayn Muhammad ibn Ahmad ibn Jubayr al-KinÄ\u0081nÄ« al-AndalusÄ«\n" +
            "                            al-BalinsÄ«.\n" +
            "                        </subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"250\">\n" +
            "                        <subfield code=\"a\">2d ed.</subfield>\n" +
            "                        <subfield code=\"b\">rev. by M. J. de Goeje and printed for the Trustees of the \"E. J. W. Gibb\n" +
            "                            memorial\"\n" +
            "                        </subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"260\">\n" +
            "                        <subfield code=\"a\">[New York,</subfield>\n" +
            "                        <subfield code=\"b\">AMS Press,</subfield>\n" +
            "                        <subfield code=\"c\">1973] 1907.</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"300\">\n" +
            "                        <subfield code=\"a\">363, 53 p.</subfield>\n" +
            "                        <subfield code=\"c\">23 cm.</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"500\">\n" +
            "                        <subfield code=\"a\">Added t.p.: The travels of Ibn Jubayr. Edited from a ms. in the University\n" +
            "                            Library of Leyden by William Wright.\n" +
            "                        </subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"500\">\n" +
            "                        <subfield code=\"a\">Original ed. issued as v. 5 of \"E.J.W. Gibb memorial\" series.</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"504\">\n" +
            "                        <subfield code=\"a\">Includes bibliographical references and index.</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\"0\" tag=\"651\">\n" +
            "                        <subfield code=\"a\">Islamic Empire</subfield>\n" +
            "                        <subfield code=\"x\">Description and travel.</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\"1\" ind2=\" \" tag=\"700\">\n" +
            "                        <subfield code=\"a\">Wright, William,</subfield>\n" +
            "                        <subfield code=\"d\">1830-1889.</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\"1\" ind2=\" \" tag=\"700\">\n" +
            "                        <subfield code=\"a\">Goeje, M. J. de</subfield>\n" +
            "                        <subfield code=\"q\">(Michael Jan),</subfield>\n" +
            "                        <subfield code=\"d\">1836-1909.</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\"0\" ind2=\" \" tag=\"740\">\n" +
            "                        <subfield code=\"a\">Travels of Ibn Jubayr.</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\"0\" tag=\"830\">\n" +
            "                        <subfield code=\"a\">\"E.J.W. Gibb memorial\" series ;</subfield>\n" +
            "                        <subfield code=\"v\">v.5.</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"907\">\n" +
            "                        <subfield code=\"a\">.b100006279</subfield>\n" +
            "                        <subfield code=\"c\">m</subfield>\n" +
            "                        <subfield code=\"d\">a</subfield>\n" +
            "                        <subfield code=\"e\">-</subfield>\n" +
            "                        <subfield code=\"f\">ara</subfield>\n" +
            "                        <subfield code=\"g\">nyu</subfield>\n" +
            "                        <subfield code=\"h\">0</subfield>\n" +
            "                        <subfield code=\"i\">3</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"952\">\n" +
            "                        <subfield code=\"h\">*OAC (\"E. J. W. Gibb memorial\" series. v. 5)</subfield>\n" +
            "                    </datafield>\n" +
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"952\">\n" +
            "                        <subfield code=\"h\">*OFV 87-659</subfield>\n" +
            "                    </datafield>\n" +
            "                    <leader>01814cam a2200409 450000</leader>\n" +
            "                </record>\n" +
            "            </collection>";

    private String holdingContent = "<collection>\n" +
            "                    <record>\n" +
            "                        <datafield ind1='0' ind2='1' tag='852'>\n" +
            "                            <subfield code='b'>rcppa</subfield>\n" +
            "                            <subfield code='t'>1</subfield>\n" +
            "                            <subfield code='h'>DF802</subfield>\n" +
            "                            <subfield code='i'>.xP45</subfield>\n" +
            "                            <subfield code='x'>tr fr anxafst</subfield>\n" +
            "                        </datafield>\n" +
            "                        <datafield ind1=' ' ind2='0' tag='866'>\n" +
            "                            <subfield code='x'>DESIGNATOR: t.</subfield>\n" +
            "                        </datafield>\n" +
            "                    </record>\n" +
            "                </collection>";

    @Test
    public void getMarcRecords() throws IOException, URISyntaxException {
        BibliographicEntity bibliographicEntity = getBibliographicEntity();
        Map<String, Object> successAndFailureFormattedList = (Map<String, Object>) marcXmlFormatterService.getFormattedOutput(Arrays.asList(bibliographicEntity));
        List<Record> recordList = readMarcXml((String) successAndFailureFormattedList.get(ReCAPConstants.DATADUMP_FORMATTEDSTRING));
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