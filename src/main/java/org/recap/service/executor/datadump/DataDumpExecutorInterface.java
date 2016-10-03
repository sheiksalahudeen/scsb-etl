package org.recap.service.executor.datadump;

import org.recap.model.export.DataDumpRequest;

import java.util.concurrent.ExecutionException;

/**
 * Created by premkb on 27/9/16.
 */
public interface DataDumpExecutorInterface {

    public boolean isInterested(String fetchType);

    public String process(DataDumpRequest dataDumpRequest) throws ExecutionException, InterruptedException;

}
