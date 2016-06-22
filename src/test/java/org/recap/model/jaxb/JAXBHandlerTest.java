package org.recap.model.jaxb;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class JAXBHandlerTest {

    @Test
    public void marshal() throws Exception {
        JAXBHandler jaxbHandler = JAXBHandler.getInstance();
        BibRecord bibRecord = new BibRecord();
        bibRecord.setOwningInstitutionId("101");
        bibRecord.setContent("marc content");
        Holding holding1 = new Holding();
        holding1.setOwningInstitutionHoldingsId("h-101");
        holding1.setContent("holding conent 1");
        Items items = new Items();
        items.setContent("item conent");
        holding1.setItems(Arrays.asList(items));
        Holding holding2 = new Holding();
        holding2.setOwningInstitutionHoldingsId("h-102");
        Holdings holdings = new Holdings();
        holdings.setHolding(Arrays.asList(holding1, holding2));
        holding2.setContent("holding content 2");
        holding2.setItems(Arrays.asList(items));
        bibRecord.setHoldings(Arrays.asList(holdings));
        jaxbHandler.marshal(bibRecord);
    }

    @Test
    public void unmarshal() throws Exception {
        JAXBHandler jaxbHandler = JAXBHandler.getInstance();

        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<bibRecord>\n" +
                "    <content>marc content</content>\n" +
                "    <holdings>\n" +
                "        <holding>\n" +
                "            <content>holding conent 1</content>\n" +
                "            <items>\n" +
                "                <content>item conent</content>\n" +
                "            </items>\n" +
                "            <owningInstitutionHoldingsId>h-101</owningInstitutionHoldingsId>\n" +
                "        </holding>\n" +
                "        <holding>\n" +
                "            <content>holding content 2</content>\n" +
                "            <items>\n" +
                "                <content>item conent</content>\n" +
                "            </items>\n" +
                "            <owningInstitutionHoldingsId>h-102</owningInstitutionHoldingsId>\n" +
                "        </holding>\n" +
                "    </holdings>\n" +
                "    <owningInstitutionId>101</owningInstitutionId>\n" +
                "</bibRecord>";


        BibRecord bibRecord = (BibRecord) jaxbHandler.unmarshal(content, BibRecord.class);

        assertNotNull(bibRecord);


    }

}