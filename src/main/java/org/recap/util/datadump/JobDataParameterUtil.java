package org.recap.util.datadump;

import org.apache.commons.collections.CollectionUtils;
import org.recap.model.jpa.JobParamDataEntity;
import org.recap.model.jpa.JobParamEntity;
import org.recap.repository.JobParamDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rajeshbabuk on 7/7/17.
 */
@Service
public class JobDataParameterUtil {

    /**
     * The Job param detail repository.
     */
    @Autowired
    JobParamDetailRepository jobParamDetailRepository;

    /**
     * This method builds the job parameters from the database and builds a map.
     *
     * @param jobName the job name
     * @return the map
     */
    public Map<String, String> buildJobRequestParameterMap(String jobName) {
        Map<String, String> parameterMap = new HashMap<>();
        JobParamEntity jobParamEntity = jobParamDetailRepository.findByJobName(jobName);
        if (CollectionUtils.isNotEmpty(jobParamEntity.getJobParamDataEntities())) {
            for (JobParamDataEntity jobParamDataEntity : jobParamEntity.getJobParamDataEntities()) {
                parameterMap.put(jobParamDataEntity.getParamName(), jobParamDataEntity.getParamValue());
            }
        }
        return parameterMap;
    }
}
