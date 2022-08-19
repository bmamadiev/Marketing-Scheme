package com.kenzie.marketing.referral.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.kenzie.marketing.referral.model.LeaderboardEntry;
import com.kenzie.marketing.referral.model.Referral;
import com.kenzie.marketing.referral.model.ReferralRequest;
import com.kenzie.marketing.referral.model.ReferralResponse;
import com.kenzie.marketing.referral.service.converter.ZonedDateTimeConverter;
import com.kenzie.marketing.referral.service.dao.ReferralDao;
import com.kenzie.marketing.referral.service.exceptions.InvalidDataException;
import com.kenzie.marketing.referral.service.model.ReferralRecord;
import net.andreinc.mockneat.MockNeat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReferralServiceTest {

    /** ------------------------------------------------------------------------
     *  expenseService.getExpenseById
     *  ------------------------------------------------------------------------ **/

    private ReferralDao referralDao;
    private ReferralService referralService;

    @BeforeAll
    void setup() {
        this.referralDao = mock(ReferralDao.class);
        this.referralService = new ReferralService(referralDao);
    }

    @Test
    void addReferralTest() {
        ArgumentCaptor<ReferralRecord> referralCaptor = ArgumentCaptor.forClass(ReferralRecord.class);

        // GIVEN
        String customerId = "fakecustomerid";
        String referrerId = "fakereferralid";
        ReferralRequest request = new ReferralRequest();
        request.setCustomerId(customerId);
        request.setReferrerId(referrerId);

        // WHEN
        ReferralResponse response = this.referralService.addReferral(request);

        // THEN
        verify(referralDao, times(1)).addReferral(referralCaptor.capture());
        ReferralRecord record = referralCaptor.getValue();

        assertNotNull(record, "The record is valid");
        assertEquals(customerId, record.getCustomerId(), "The record customerId should match");
        assertEquals(referrerId, record.getReferrerId(), "The record referrerId should match");
        assertNotNull(record.getDateReferred(), "The record referral date exists");

        assertNotNull(response, "A response is returned");
        assertEquals(customerId, response.getCustomerId(), "The response customerId should match");
        assertEquals(referrerId, response.getReferrerId(), "The response referrerId should match");
        assertNotNull(response.getReferralDate(), "The response referral date exists");
    }

    @Test
    void addReferralTest_no_customer_id() {
        // GIVEN
        String customerId = "";
        String referrerId = "";
        ReferralRequest request = new ReferralRequest();
        request.setCustomerId(customerId);
        request.setReferrerId(referrerId);

        // WHEN / THEN
        assertThrows(InvalidDataException.class, ()->this.referralService.addReferral(request));
    }

    @Test
    void getDirectReferralsTest() {
        // GIVEN
        String customerId = "fakecustomerid";
        List<ReferralRecord> recordList = new ArrayList<>();

        ReferralRecord record1 = new ReferralRecord();
        record1.setCustomerId("customer1");
        record1.setReferrerId(customerId);
        record1.setDateReferred(ZonedDateTime.now());
        recordList.add(record1);

        ReferralRecord record2 = new ReferralRecord();
        record2.setCustomerId("customer2");
        record2.setReferrerId(customerId);
        record2.setDateReferred(ZonedDateTime.now());
        recordList.add(record2);

        when(referralDao.findByReferrerId(customerId)).thenReturn(recordList);

        // WHEN
        List<Referral> referrals = this.referralService.getDirectReferrals(customerId);

        // THEN
        verify(referralDao, times(1)).findByReferrerId(customerId);

        assertNotNull(referrals, "The returned referral list is valid");
        assertEquals(2, referrals.size(), "The referral list has 2 items");
        for (Referral referral : referrals) {
            if (record1.getCustomerId().equals(referral.getCustomerId())) {
                assertEquals(record1.getReferrerId(), customerId);
                assertEquals(new ZonedDateTimeConverter().convert(record1.getDateReferred()), referral.getReferralDate());
            } else if (record2.getCustomerId().equals(referral.getCustomerId())) {
                assertEquals(record2.getReferrerId(), customerId);
                assertEquals(new ZonedDateTimeConverter().convert(record2.getDateReferred()), referral.getReferralDate());
            } else {
                fail("A Referral was returned that does not match record 1 or 2.");
            }
        }
    }

    // Write additional tests here
    @Test
    public void getReferralsLeaders() throws Exception {
        // GIVEN
        String customerId1 = "fakecustomerid1";
        List<ReferralRecord> recordList1 = new ArrayList<>();

        ReferralRecord record1 = new ReferralRecord();
        record1.setCustomerId("customer1");
        record1.setReferrerId(Optional.empty().toString());
        record1.setDateReferred(ZonedDateTime.now());
        recordList1.add(record1);

        ReferralRecord record2 = new ReferralRecord();
        record2.setCustomerId("customer2");
        record2.setReferrerId(customerId1);
        record2.setDateReferred(ZonedDateTime.now());
        recordList1.add(record2);

        ReferralRecord record3 = new ReferralRecord();
        record3.setCustomerId("customer3");
        record3.setReferrerId(customerId1);
        record3.setDateReferred(ZonedDateTime.now());
        recordList1.add(record3);

        ReferralRecord record4 = new ReferralRecord();
        record4.setCustomerId("customer4");
        record4.setReferrerId(customerId1);
        record4.setDateReferred(ZonedDateTime.now());
        recordList1.add(record4);



        String customerId2 = "fakecustomerid2";
        List<ReferralRecord> recordList2 = new ArrayList<>();

        ReferralRecord record01 = new ReferralRecord();
        record01.setCustomerId("customer01");
        record01.setReferrerId(customerId2);
        record01.setDateReferred(ZonedDateTime.now());
        recordList2.add(record01);

        ReferralRecord record02 = new ReferralRecord();
        record02.setCustomerId("customer02");
        record02.setReferrerId(customerId2);
        record02.setDateReferred(ZonedDateTime.now());
        recordList2.add(record02);

        ReferralRecord record03 = new ReferralRecord();
        record03.setCustomerId("customer03");
        record03.setReferrerId(customerId2);
        record03.setDateReferred(ZonedDateTime.now());
        recordList2.add(record03);

        ReferralRecord record04 = new ReferralRecord();
        record04.setCustomerId("customer04");
        record04.setReferrerId(customerId2);
        record04.setDateReferred(ZonedDateTime.now());
        recordList2.add(record04);



        String customerId3 = "fakecustomerid3";
        List<ReferralRecord> recordList3 = new ArrayList<>();

        ReferralRecord record001 = new ReferralRecord();
        record001.setCustomerId("customer001");
        record001.setReferrerId(customerId3);
        record001.setDateReferred(ZonedDateTime.now());
        recordList3.add(record001);

        ReferralRecord record002 = new ReferralRecord();
        record002.setCustomerId("customer002");
        record002.setReferrerId(customerId3);
        record002.setDateReferred(ZonedDateTime.now());
        recordList3.add(record002);

        ReferralRecord record003 = new ReferralRecord();
        record003.setCustomerId("customer003");
        record003.setReferrerId(customerId3);
        record003.setDateReferred(ZonedDateTime.now());
        recordList3.add(record003);

        ReferralRecord record004 = new ReferralRecord();
        record004.setCustomerId("customer004");
        record004.setReferrerId(customerId3);
        record004.setDateReferred(ZonedDateTime.now());
        recordList3.add(record004);

        ReferralRecord record005 = new ReferralRecord();
        record005.setCustomerId("customer005");
        record005.setReferrerId(customerId3);
        record005.setDateReferred(ZonedDateTime.now());
        recordList3.add(record005);



        String customerId4 = "fakecustomerid4";
        List<ReferralRecord> recordList4 = new ArrayList<>();

        ReferralRecord record0001 = new ReferralRecord();
        record0001.setCustomerId("customer0001");
        record0001.setReferrerId(customerId4);
        record0001.setDateReferred(ZonedDateTime.now());
        recordList4.add(record0001);

        ReferralRecord record0002 = new ReferralRecord();
        record0002.setCustomerId("customer0002");
        record0002.setReferrerId(customerId4);
        record0002.setDateReferred(ZonedDateTime.now());
        recordList4.add(record0002);

        // WHEN
        //List<LeaderboardEntry> leaderBoard = referralService.getReferralLeaderboard();

        // THEN

    }

}