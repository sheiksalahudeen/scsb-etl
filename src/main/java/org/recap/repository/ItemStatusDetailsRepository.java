package org.recap.repository;

import org.recap.model.jpa.ItemStatusEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by pvsubrah on 6/27/16.
 */
public interface ItemStatusDetailsRepository extends PagingAndSortingRepository<ItemStatusEntity, Integer> {

    ItemStatusEntity findByStatusCode(String statusCode);

}
