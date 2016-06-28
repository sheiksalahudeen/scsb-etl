package org.recap.repository;

import org.recap.model.jpa.CollectionGroupEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by angelind on 27/6/16.
 */
public interface CollectionGroupDetailsRepository extends CrudRepository<CollectionGroupEntity, Integer> {
    CollectionGroupEntity findByCollectionGroupCode(String collectionGroupCode);
}
