package org.recap.camel.datadump;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.repository.BibliographicDetailsRepository;

import java.util.*;

/**
 * Created by peris on 11/1/16.
 */

public class SolrSearchResultsProcessorForExport implements Processor {
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    public SolrSearchResultsProcessorForExport(BibliographicDetailsRepository bibliographicDetailsRepository) {
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        Map results = (Map) exchange.getIn().getBody();
        List<HashMap> dataDumpSearchResults = (List<HashMap>) results.get("dataDumpSearchResults");

        List<BibliographicEntity> bibliographicEntities = new ArrayList<>();

        for (Iterator<HashMap> iterator = dataDumpSearchResults.iterator(); iterator.hasNext(); ) {
            HashMap linkedHashMap = iterator.next();
            Integer bibId = (Integer) linkedHashMap.get("bibId");
            List<Integer> itemIds = (List<Integer>) linkedHashMap.get("itemIds");
            List<BibliographicEntity> bibliographicEntityList = bibliographicDetailsRepository.getBibliographicEntityList(Arrays.asList(bibId));
            BibliographicEntity bibliographicEntity = bibliographicEntityList.get(0);
            bibliographicEntity.getItemEntities();
            for (Iterator<BibliographicEntity> bibliographicEntityIterator = bibliographicEntityList.iterator(); bibliographicEntityIterator.hasNext(); ) {
                BibliographicEntity retrievedBibEntity = bibliographicEntityIterator.next();
                List<ItemEntity> itemEntities = retrievedBibEntity.getItemEntities();

                List<ItemEntity> filteredItems = new ArrayList<>();
                for (Iterator<ItemEntity> itemEntityIterator = itemEntities.iterator(); itemEntityIterator.hasNext(); ) {
                    ItemEntity itemEntity = itemEntityIterator.next();
                    if (itemIds.contains(itemEntity.getItemId())) {
                        filteredItems.add(itemEntity);
                    }
                }

                bibliographicEntity.setItemEntities(filteredItems);
                bibliographicEntities.add(bibliographicEntity);
            }
        }

        exchange.getOut().setBody(bibliographicEntities);
        exchange.getOut().setHeader("fileName", exchange.getIn().getHeader("fileName"));
    }
}
