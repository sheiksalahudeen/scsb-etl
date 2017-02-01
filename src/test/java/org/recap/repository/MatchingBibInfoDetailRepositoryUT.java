package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.MatchingBibInfoDetail;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by premkb on 29/1/17.
 */
public class MatchingBibInfoDetailRepositoryUT extends BaseTestCase {

    @Autowired
    private MatchingBibInfoDetailRepository matchingBibInfoDetailRepository;

    @Test
    public void getRecordNum(){
        ArrayList<String> bibIdList = new ArrayList<>();
        bibIdList.add("7");
        bibIdList.add("9");
        List<Integer> recordNumList = matchingBibInfoDetailRepository.getRecordNum(bibIdList);
        assertNotNull(recordNumList);
    }

    @Test
    public void findByRecordNum(){
        ArrayList<Integer> recordNumList = new ArrayList<>();
        recordNumList.add(1);
        recordNumList.add(2);
        List<MatchingBibInfoDetail> matchingBibInfoDetailList = matchingBibInfoDetailRepository.findByRecordNum(recordNumList);
        assertNotNull(matchingBibInfoDetailList);
    }
}
