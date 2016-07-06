package org.recap.route;

import org.recap.model.jpa.BibliographicEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchulakshmig on 4/7/16.
 */
public class ETLExchange implements Serializable {

    private List<BibliographicEntity> bibliographicEntities;
    private Map institutionEntityMap;
    private Map collectionGroupMap;

    public List<BibliographicEntity> getBibliographicEntities() {
        return bibliographicEntities;
    }

    public void setBibliographicEntities(List<BibliographicEntity> bibliographicEntities) {
        this.bibliographicEntities = bibliographicEntities;
    }

    public Map getInstitutionEntityMap() {
        return institutionEntityMap;
    }

    public void setInstitutionEntityMap(Map institutionEntityMap) {
        this.institutionEntityMap = institutionEntityMap;
    }

    public Map getCollectionGroupMap() {
        return collectionGroupMap;
    }

    public void setCollectionGroupMap(Map collectionGroupMap) {
        this.collectionGroupMap = collectionGroupMap;
    }
}
