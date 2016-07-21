package org.recap.repository;

import org.recap.model.jpa.HoldingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by chenchulakshmig on 6/13/16.
 */
public interface HoldingsDetailsRepository extends JpaRepository<HoldingsEntity, String> {

    HoldingsEntity findByHoldingsId(Integer holdingsId);

}
