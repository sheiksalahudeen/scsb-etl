package org.recap.route;

import org.apache.camel.component.leveldb.LevelDBAggregationRepository;
import org.apache.camel.component.leveldb.LevelDBFile;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository;
import org.apache.camel.spi.AggregationRepository;

/**
 * Created by peris on 7/15/16.
 */
public class ReCAPJDBCAggregationRepository extends LevelDBAggregationRepository {

    public ReCAPJDBCAggregationRepository() {
        super();
    }

    public ReCAPJDBCAggregationRepository(String repositoryName) {
        super(repositoryName);
    }

    public ReCAPJDBCAggregationRepository(String repositoryName, String persistentFileName) {
        super(repositoryName, persistentFileName);
    }

    public ReCAPJDBCAggregationRepository(String repositoryName, LevelDBFile levelDBFile) {
        super(repositoryName, levelDBFile);
    }
}
