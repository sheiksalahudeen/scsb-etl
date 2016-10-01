package org.recap.service.transmission.datadump;

import org.recap.model.export.DataDumpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by premkb on 28/9/16.
 */
@Service
public class DataDumpTransmissionService {

    private List<DataDumpTransmissionInterface> dataDumpTransmissionInterfaceList;

    @Autowired
    private DataDumpFileSystemTranmissionService dataDumpFileSystemTranmissionService;

    @Autowired
    private DataDumpFtpTransmissionService dataDumpFtpTransmissionService;


    public void starTranmission(Object formattedObject, DataDumpRequest dataDumpRequest, Map<String,String> routeMap){
        for(DataDumpTransmissionInterface dataDumpTransmissionInterface:getTransmissionService()){
            if(dataDumpTransmissionInterface.isInterested(dataDumpRequest)){
                dataDumpTransmissionInterface.transmitDataDump(formattedObject,routeMap);
            }
        }
    }

    public List<DataDumpTransmissionInterface> getTransmissionService(){
        if(CollectionUtils.isEmpty(dataDumpTransmissionInterfaceList)){
            dataDumpTransmissionInterfaceList = new ArrayList<>();
            dataDumpTransmissionInterfaceList.add(dataDumpFileSystemTranmissionService);
            dataDumpTransmissionInterfaceList.add(dataDumpFtpTransmissionService);
        }
        return dataDumpTransmissionInterfaceList;
    }
}
