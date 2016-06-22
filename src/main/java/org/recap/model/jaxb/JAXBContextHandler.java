package org.recap.model.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class JAXBContextHandler {
    private Map<String, JAXBContext> contextMap;

    public JAXBContext getJAXBContextForClass(Class cl) throws JAXBException {
        if(getContextMap().containsKey(cl)){
            return contextMap.get(cl.getName());
        } else {
            JAXBContext newInstance = JAXBContext.newInstance(cl);
            getContextMap().put(cl.getName(), newInstance);
        }

        return contextMap.get(cl.getName());
    }

    public Map<String, JAXBContext> getContextMap() {
        if (null == contextMap) {
            contextMap = new HashMap<>();
        }
        return contextMap;
    }

    public void setContextMap(Map<String, JAXBContext> contextMap) {
        this.contextMap = contextMap;
    }
}
