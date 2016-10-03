package org.recap.service.formatter.datadump;

import org.recap.model.jpa.BibliographicEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by premkb on 28/9/16.
 */
@Service
public class DataDumpFormatterService {

    private List<DataDumpFormatterInterface> dataDumpFormatterInterfaceList;
    @Autowired
    private MarcXmlFormatterService marcXmlFormatterService;
    @Autowired
    private SCSBXmlFormatterService scsbXmlFormatterService;
    @Autowired
    private DeletedJsonFormatterService deletedJsonFormatterService;

    public Object getFormattedObject(List<BibliographicEntity> bibliographicEntityList, String outputFormat){
        Object formatterObject = null;
        for(DataDumpFormatterInterface dataDumpFormatterInterface:getDataDumpFormatter()){
            if(dataDumpFormatterInterface.isInterested(outputFormat)){
                formatterObject = dataDumpFormatterInterface.getFormattedOutput(bibliographicEntityList);
            }
        }
        return formatterObject;
    }

    public List<DataDumpFormatterInterface> getDataDumpFormatter(){
        if(CollectionUtils.isEmpty(dataDumpFormatterInterfaceList)){
            dataDumpFormatterInterfaceList = new ArrayList<>();
            dataDumpFormatterInterfaceList.add(marcXmlFormatterService);
            dataDumpFormatterInterfaceList.add(scsbXmlFormatterService);
            dataDumpFormatterInterfaceList.add(deletedJsonFormatterService);
        }
        return dataDumpFormatterInterfaceList;
    }
}
