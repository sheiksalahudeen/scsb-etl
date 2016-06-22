package org.recap.repository;

import org.recap.model.jpa.HoldingsEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chenchulakshmig on 6/13/16.
 */
public interface HoldingsDetailsRepository extends CrudRepository<HoldingsEntity, Integer> {
    public List<HoldingsEntity> findAllByHoldingsId(Integer holdingsId);
}
