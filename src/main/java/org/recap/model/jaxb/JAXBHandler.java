package org.recap.model.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class JAXBHandler {

    private static JAXBHandler jaxbHandler;

    private JAXBHandler() {

    }

    public static JAXBHandler getInstance() {
        if (null == jaxbHandler) {
            jaxbHandler = new JAXBHandler();
        }
        return jaxbHandler;
    }

    public static void marshal(BibRecord bibRecord) {
        try {
            JAXBContext jaxbContext = JAXBContextHandler.getInstance().getJAXBContextForClass(BibRecord.class);

            Marshaller marshaller = jaxbContext.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            marshaller.marshal(bibRecord, System.out);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }


    public Object unmarshal(String content, Class cl) {
        BibRecord bibRecord = null;
        try {
            JAXBContext jaxbContextForClass = JAXBContextHandler.getInstance().getJAXBContextForClass(cl);

            Unmarshaller unmarshaller = jaxbContextForClass.createUnmarshaller();

            bibRecord = (BibRecord) unmarshaller.unmarshal(new StringReader(content));

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return bibRecord;
    }
}
