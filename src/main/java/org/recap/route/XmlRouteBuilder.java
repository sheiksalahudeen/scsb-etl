package org.recap.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.apache.commons.io.FilenameUtils;
import org.recap.repository.XmlRecordRepository;

/**
 * Created by angelind on 21/7/16.
 */
public class XmlRouteBuilder extends RouteBuilder {

    private String xmlTagName;
    private String inputDirectoryPath;
    private Integer poolSize;
    private Integer maxPoolSize;
    private XmlRecordRepository xmlRecordRepository;

    public String getXmlTagName() {
        return xmlTagName;
    }

    public void setXmlTagName(String xmlTagName) {
        this.xmlTagName = xmlTagName;
    }

    public String getInputDirectoryPath() {
        return inputDirectoryPath;
    }

    public void setInputDirectoryPath(String inputDirectoryPath) {
        this.inputDirectoryPath = inputDirectoryPath;
    }

    public Integer getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(Integer poolSize) {
        this.poolSize = poolSize;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public XmlRecordRepository getXmlRecordRepository() {
        return xmlRecordRepository;
    }

    public void setXmlRecordRepository(XmlRecordRepository xmlRecordRepository) {
        this.xmlRecordRepository = xmlRecordRepository;
    }

    @Override
    public void configure() throws Exception {
        FileEndpoint fileEndpoint = endpoint("file:" + inputDirectoryPath, FileEndpoint.class);
        fileEndpoint.setFilter(new XmlFileFilter());

        from(fileEndpoint)
                .split()
                .tokenizeXML(xmlTagName)
                .streaming()
                .parallelProcessing().threads(poolSize, maxPoolSize)
                .process(new XmlProcessor(xmlRecordRepository));
    }

    class XmlFileFilter implements GenericFileFilter {
        @Override
        public boolean accept(GenericFile file) {
            return FilenameUtils.getExtension(file.getAbsoluteFilePath()).equalsIgnoreCase("xml");
        }
    }
}
