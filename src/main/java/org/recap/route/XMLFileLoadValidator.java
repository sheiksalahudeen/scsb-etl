package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultMessage;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by peris on 8/20/16.
 */
@Component
public class XMLFileLoadValidator implements Processor {

    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Override
    public void process(Exchange exchange) throws Exception {
        String camelFileName = (String) exchange.getIn().getHeader("CamelFileName");

        List<ReportEntity> reportEntity =
                reportDetailRepository.findByFileName(camelFileName);

        if(!CollectionUtils.isEmpty(reportEntity)){
            DefaultMessage defaultMessage = new DefaultMessage();
            defaultMessage.setBody("");
            exchange.setIn(defaultMessage);
            exchange.setOut(defaultMessage);
        }
    }
}
