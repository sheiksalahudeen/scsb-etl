package org.recap.repository;

import org.recap.model.jpa.HoldingsEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by chenchulakshmig on 6/13/16.
 */
public interface HoldingsDetailsRepository extends PagingAndSortingRepository<HoldingsEntity, Integer> {

    HoldingsEntity findByHoldingsId(Integer holdingsId);

}
