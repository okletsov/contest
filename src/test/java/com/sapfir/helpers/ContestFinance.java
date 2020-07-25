package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;

public class ContestFinance {

    private static final Logger Log = LogManager.getLogger(ContestFinance.class.getName());
    private final Connection conn;
    private final String contestId;

    public ContestFinance(Connection conn, String contestId) {
        this.conn = conn;
        this.contestId = contestId;
    }

    private BigDecimal getSumEntranceFees() {

        Contest c = new Contest(conn, contestId);

        String contestType = c.getContestType();
        String seasContestId;
        BigDecimal entranceFee;

        if (contestType.equals("seasonal")) {
            seasContestId = contestId;
            entranceFee = c.getEntranceFee();
        } else {
//            If the contest passed in during initialization is monthly, we still need to get its parent seasonal
//            contest id in order to get participants count and calculate sum of entrance fees

            seasContestId = c.getSeasContestIdByMonContestId();

            Contest cSeas = new Contest(conn, seasContestId);
            entranceFee = cSeas.getEntranceFee();
        }

        BigDecimal participantsCount = BigDecimal.valueOf(c.getParticipantsCount(seasContestId));
        return  entranceFee.multiply(participantsCount);
    }

    private BigDecimal seasPrize() {
        return getSumEntranceFees().multiply(BigDecimal.valueOf(0.8));
    }

    private BigDecimal getAnnPrize() {

        BigDecimal annPrize = BigDecimal.valueOf(0);

        Contest c = new Contest(conn, contestId);
        ArrayList<String> contestIds = c.getSeasIdsForAnnContest();

        for (String id : contestIds) {
            ContestFinance cf = new ContestFinance(conn, id);
            annPrize = annPrize.add(cf.getSumEntranceFees().multiply(BigDecimal.valueOf(0.2)));
        }

        return annPrize;
    }

    private BigDecimal getSeasPlacesPrize() {

        Contest c = new Contest(conn, contestId);
        String mon1ContestId = c.getMonContestId(1);
        String mon2ContestId = c.getMonContestId(2);

        int mon1Participants = c.getParticipantsCount(mon1ContestId);
        int mon2Participants = c.getParticipantsCount(mon2ContestId);

        if (mon1Participants == 0 && mon2Participants == 0) { return seasPrize().multiply(BigDecimal.valueOf(0.9)); }
        if (mon1Participants > 0 && mon2Participants == 0) { return seasPrize().multiply(BigDecimal.valueOf(0.7)); }
        if (mon1Participants == 0 && mon2Participants > 0) { return seasPrize().multiply(BigDecimal.valueOf(0.7)); }
        return seasPrize().multiply(BigDecimal.valueOf(0.5));
    }

    private BigDecimal getSeasFirstPlaceAward() {
        return getSeasPlacesPrize().multiply(BigDecimal.valueOf(0.5));
    }

    private BigDecimal getSeasSecondPlaceAward() {
        return getSeasPlacesPrize().multiply(BigDecimal.valueOf(0.3));
    }

    private BigDecimal getSeasThirdPlaceAward() {
        return getSeasPlacesPrize().multiply(BigDecimal.valueOf(0.2));
    }

    public BigDecimal getWinningStrickAward() {
        return  seasPrize().multiply(BigDecimal.valueOf(0.05));
    }

    public BigDecimal getBiggestOddsAward() {
        return seasPrize().multiply(BigDecimal.valueOf(0.05));
    }

    private BigDecimal getMonPlacesPrize() {
        return seasPrize().multiply(BigDecimal.valueOf(0.2));
    }

    private BigDecimal getMonFirstPlaceAward(int participants) {
        if (participants >= 3) { return getMonPlacesPrize().multiply(BigDecimal.valueOf(0.5)); }
        if (participants == 2) { return getMonPlacesPrize().multiply(BigDecimal.valueOf(0.6)); }
        if (participants == 1) { return getMonPlacesPrize(); }
        return BigDecimal.valueOf(0);
    }

    private BigDecimal getMonSecondPlaceAward(int participants) {
        if (participants >= 3) { return getMonPlacesPrize().multiply(BigDecimal.valueOf(0.3)); }
        if (participants == 2) { return getMonPlacesPrize().multiply(BigDecimal.valueOf(0.4)); }
        return BigDecimal.valueOf(0);
    }

    private BigDecimal getMonThirdPlaceAward(int participants) {
        if (participants >= 3) { return getMonPlacesPrize().multiply(BigDecimal.valueOf(0.2)); }
        return BigDecimal.valueOf(0);
    }

    public BigDecimal getFinanceActionValue(int place, String contestId) {
        Contest c = new Contest(conn, contestId);

        String contestType = c.getContestType();

        if (contestType.equals("seasonal") && place == 1) { return getSeasFirstPlaceAward(); }
        if (contestType.equals("seasonal") && place == 2) { return getSeasSecondPlaceAward(); }
        if (contestType.equals("seasonal") && place == 3) { return getSeasThirdPlaceAward(); }
        if (contestType.equals("monthly") && place == 1) { return getMonFirstPlaceAward(c.getParticipantsCount(contestId)); }
        if (contestType.equals("monthly") && place == 2) { return getMonSecondPlaceAward(c.getParticipantsCount(contestId)); }
        if (contestType.equals("monthly") && place == 3) { return getMonThirdPlaceAward(c.getParticipantsCount(contestId)); }
        return BigDecimal.valueOf(0);

    }

    private BigDecimal getAnnFirstPlaceAward() {
        return getAnnPrize().multiply(BigDecimal.valueOf(0.5));
    }

    private BigDecimal getAnnSecondPlaceAward() {
        return getAnnPrize().multiply(BigDecimal.valueOf(0.3));
    }

    private BigDecimal getAnnThirdPlaceAward() {
        return getAnnPrize().multiply(BigDecimal.valueOf(0.2));
    }

    public BigDecimal getAnnFinanceActionValue(int place) {
        if (place == 1) { return getAnnFirstPlaceAward(); }
        if (place == 2) { return getAnnSecondPlaceAward(); }
        if (place == 3) { return getAnnThirdPlaceAward(); }
        return BigDecimal.valueOf(0);
    }

    public int getFinanceActionId(int place, String contestId) {
        Contest c = new Contest(conn, contestId);

        String contestType = c.getContestType();

        if (contestType.equals("seasonal") && place == 1) { return 1; }
        if (contestType.equals("seasonal") && place == 2) { return 2; }
        if (contestType.equals("seasonal") && place == 3) { return 3; }
        if (contestType.equals("monthly") && place == 1) { return 4; }
        if (contestType.equals("monthly") && place == 2) { return 5; }
        if (contestType.equals("monthly") && place == 3) { return 6; }

        return 0;
    }

}
