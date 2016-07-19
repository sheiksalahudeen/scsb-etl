package org.recap.model.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

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

    private static Map<String, Unmarshaller> unmarshallerMap;

    private static Marshaller marshaller;

    public static void marshal(BibRecord bibRecord) {
        try {
            Marshaller marshaller = getMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            marshaller.marshal(bibRecord, System.out);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public Object unmarshal(String content, Class cl) {
        BibRecord bibRecord = null;
        try {
            Unmarshaller unmarshaller = getUnmarshaller(cl);
            bibRecord = (BibRecord) unmarshaller.unmarshal(new StringReader(content));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return bibRecord;
    }

    private Unmarshaller getUnmarshaller(Class cl) throws JAXBException {
        if (getUnmarshallerMap().containsKey(cl.getName())) {
            return getUnmarshallerMap().get(cl.getName());
        } else {
            JAXBContext jaxbContextForClass = JAXBContextHandler.getInstance().getJAXBContextForClass(cl);
            Unmarshaller unmarshaller = jaxbContextForClass.createUnmarshaller();
            getUnmarshallerMap().put(cl.getName(), unmarshaller);
        }
        return getUnmarshallerMap().get(cl.getName());
    }

    public Map<String, Unmarshaller> getUnmarshallerMap() {
        if (null == unmarshallerMap) {
            unmarshallerMap = new HashMap<>();
        }
        return unmarshallerMap;
    }

    public void setUnmarshallerMap(Map<String, Unmarshaller> unmarshallerMap) {
        this.unmarshallerMap = unmarshallerMap;
    }

    private static Marshaller getMarshaller() {
        if (marshaller == null) {
            try {
                JAXBContext jaxbContext = JAXBContextHandler.getInstance().getJAXBContextForClass(BibRecord.class);
                marshaller = jaxbContext.createMarshaller();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        return marshaller;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }
}
