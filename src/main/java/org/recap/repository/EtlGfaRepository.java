package org.recap.repository;

import org.recap.model.jpa.EtlGfaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by hemalathas on 21/7/16.
 */
public interface EtlGfaRepository extends JpaRepository<EtlGfaEntity,Integer>{

    List<EtlGfaEntity> findBystatus(String status);
    List<EtlGfaEntity> findByItemBarcode(String itemBarcode);


}
