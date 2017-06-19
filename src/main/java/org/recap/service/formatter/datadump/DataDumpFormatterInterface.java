package org.recap.service.formatter.datadump;

/**
 * Created by premkb on 28/9/16.
 */
@FunctionalInterface
public interface DataDumpFormatterInterface {

    /**
     * Is interested boolean.
     *
     * @param formatType the format type
     * @return the boolean
     */
    public boolean isInterested(String formatType);


}
