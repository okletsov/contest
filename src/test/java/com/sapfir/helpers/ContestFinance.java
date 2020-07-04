package com.sapfir.helpers;

import java.math.BigDecimal;
import java.sql.Connection;

public class ContestFinance {

    private final Connection conn;
    private final String contestId;

    public ContestFinance(Connection conn, String contestId) {
        this.conn = conn;
        this.contestId = contestId;
    }

    public BigDecimal getSumEntranceFees() {

        Contest c = new Contest(conn, contestId);
        BigDecimal participantsCount = BigDecimal.valueOf(c.getParticipantsCount(contestId));

        return c.getEntranceFee().multiply(participantsCount);

    }

    public BigDecimal seasPrize() {
        return getSumEntranceFees().multiply(BigDecimal.valueOf(0.8));
    }

    public BigDecimal getSeasPlacesPrize() {

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

    public BigDecimal getSeasFirstPlaceAward() {
        return getSeasPlacesPrize().multiply(BigDecimal.valueOf(0.5));
    }

    public BigDecimal getSeasSecondPlaceAward() {
        return getSeasPlacesPrize().multiply(BigDecimal.valueOf(0.3));
    }

    public BigDecimal getSeasThirdPlaceAward() {
        return getSeasPlacesPrize().multiply(BigDecimal.valueOf(0.2));
    }

    public BigDecimal getWinningStrickAward() {
        return  seasPrize().multiply(BigDecimal.valueOf(0.05));
    }

    public BigDecimal getBiggestOddsAward() {
        return seasPrize().multiply(BigDecimal.valueOf(0.05));
    }

    public BigDecimal getMonPlacesPrize() {
        return seasPrize().multiply(BigDecimal.valueOf(0.2));
    }

    public BigDecimal getMonFirstPlaceAward(int participants) {
        if (participants >= 3) { return getMonPlacesPrize().multiply(BigDecimal.valueOf(0.5)); }
        if (participants == 2) { return getMonPlacesPrize().multiply(BigDecimal.valueOf(0.6)); }
        if (participants == 1) { return getMonPlacesPrize(); }
        return BigDecimal.valueOf(0);
    }

    public BigDecimal getMonSecondPlaceAward(int participants) {
        if (participants >= 3) { return getMonPlacesPrize().multiply(BigDecimal.valueOf(0.3)); }
        if (participants == 2) { return getMonPlacesPrize().multiply(BigDecimal.valueOf(0.4)); }
        return BigDecimal.valueOf(0);
    }

    public BigDecimal getMonThirdPlaceAward(int participants) {
        if (participants >= 3) { return getMonPlacesPrize().multiply(BigDecimal.valueOf(0.2)); }
        return BigDecimal.valueOf(0);
    }

}
