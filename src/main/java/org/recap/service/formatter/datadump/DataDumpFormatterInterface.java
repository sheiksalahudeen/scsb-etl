package org.recap.service.formatter.datadump;

import org.recap.model.jpa.BibliographicEntity;

import java.util.List;

/**
 * Created by premkb on 28/9/16.
 */
public interface DataDumpFormatterInterface {

    public boolean isInterested(String formatType);

    public Object getFormattedOutput(List<BibliographicEntity> bibliographicEntityList);
}
