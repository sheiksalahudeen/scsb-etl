package org.recap.repository;

import org.recap.model.jpa.ItemStatusEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by pvsubrah on 6/27/16.
 */
public interface ItemStatusDetailsRepository extends PagingAndSortingRepository<ItemStatusEntity, Integer> {

    /**
     * Find by status code item status entity.
     *
     * @param statusCode the status code
     * @return the item status entity
     */
    ItemStatusEntity findByStatusCode(String statusCode);

}
