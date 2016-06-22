package org.recap.model.jaxb;

import org.junit.Test;

import java.util.Arrays;

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

}