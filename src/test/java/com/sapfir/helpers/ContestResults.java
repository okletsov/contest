package com.sapfir.helpers;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContestResults {

    private final Connection conn;

    public ContestResults(Connection conn) {
        this.conn = conn;
    }

    public int getAnnualPointsByPlaceAndParticipants(int place, int participants) {
        if (place > 2) {
            return participants - place + 1;
        } else if (place == 2) {
            return participants - place + 2;
        } else if (place == 1) {
            return participants - place + 3;
        } else if (place == 0) { // is true only when participant is on the last place
            return 1;
        } else {
            return 0;
        }
    }

    public List<HashMap<String,Object>> getGeneralContestResultsToWrite(String contestId) {

        Contest c = new Contest(conn, contestId);
        DatabaseOperations dbOp = new DatabaseOperations();

        String contestType = c.getContestType();

        HashMap<String, Object> paramValues = new HashMap<>();
        paramValues.put("contest_id", contestId);

        String sql = null;

        switch (contestType) {
            case "seasonal": {
                SqlLoader sqlLoader = new SqlLoader("sql/cr_to_write_seasonal.sql");
                sql = sqlLoader.getSql(paramValues);
                break;
            }
            case "monthly": {
                SqlLoader sqlLoader = new SqlLoader("sql/cr_to_write_monthly.sql");
                sql = sqlLoader.getSql(paramValues);

                break;
            }
            case "annual": {

                ArrayList<String> seasIdsForAnnContest = c.getSeasIdsForAnnContest();
                paramValues.put("seas_ids", seasIdsForAnnContest);

                SqlLoader sqlLoader = new SqlLoader("sql/cr_to_write_annual.sql");
                sql = sqlLoader.getSql(paramValues);
                break;
            }
        }

        return dbOp.getListOfHashMaps(conn, sql);

    }

    public List<HashMap<String,Object>> getContestResultsWinningStrickToWrite(String contestId) {
        DatabaseOperations dbOp = new DatabaseOperations();

        String sql = "select \n" +
                "\tt5.nickname\n" +
                "\t, t5.user_id\n" +
                "\t, t5.contest_id\n" +
                "\t, t5.strick_avg_odds\n" +
                "\t, t5.strick_length\n" +
                "from (\n" +
                "\tselect \n" +
                "\t\t(\n" +
                "\t\t\trow_number() over (partition by\n" +
                "\t\t\t\t\t\t\t\t\tt4.nickname\n" +
                "\t\t\t\t\t\t\t\torder by \n" +
                "\t\t\t\t\t\t\t\t\tt4.strick_length desc\n" +
                "\t\t\t\t\t\t\t\t\t, t4.strick_avg_odds desc\n" +
                "\t\t\t\t\t\t\t\t)\n" +
                "\t\t) as strick_rank\n" +
                "\t\t, t4.nickname\n" +
                "\t\t, t4.user_id\n" +
                "\t\t, t4.seasonal_contest_id as contest_id\n" +
                "\t\t, t4.strick_avg_odds\n" +
                "\t\t, t4.strick_length\n" +
                "\tfrom (\n" +
                "\t\tselect \n" +
                "\t\t\tt3.nickname\n" +
                "\t\t\t, t3.user_id \n" +
                "\t\t\t, t3.seasonal_contest_id \n" +
                "\t\t\t, round(avg(t3.user_pick_value), 2) as strick_avg_odds\n" +
                "\t\t\t, count(t3.id) as strick_length\n" +
                "\t\tfrom (\n" +
                "\t\t\tselect \n" +
                "\t\t\t\t(\n" +
                "\t\t\t\t\trow_number() over (order by \n" +
                "\t\t\t\t\t\t\t\t\t\t\tp3.user_id\n" +
                "\t\t\t\t\t\t\t\t\t\t\t, case when t2.initial_date_scheduled is null then 1 else 0 end\n" +
                "\t\t\t\t\t\t\t\t\t\t\t, t2.initial_date_scheduled asc\n" +
                "\t\t\t\t\t\t\t\t\t\t\t, t2.date_predicted asc) -\n" +
                "\t\t\t\t\trow_number() over (partition by \n" +
                "\t\t\t\t\t\t\t\t\t\t\tp3.user_id\n" +
                "\t\t\t\t\t\t\t\t\t\t\t, case \n" +
                "\t\t\t\t\t\t\t\t\t\t\t\twhen vs.count_lost = 1 and vs.count_in_contest = 1 then 'lost'\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\twhen vs.count_void = 1 and vs.count_in_contest = 1 then 'void'\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\twhen p3.result = 'void-won' then 'won'\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\twhen p3.result = 'void-lost' then 'lost'\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\telse p3.result\n" +
                "\t\t\t\t\t\t\t\t\t\t\tend\n" +
                "\t\t\t\t\t\t\t\t\t\torder by \n" +
                "\t\t\t\t\t\t\t\t\t\t\tp3.user_id \n" +
                "\t\t\t\t\t\t\t\t\t\t\t, case when t2.initial_date_scheduled is null then 1 else 0 end\n" +
                "\t\t\t\t\t\t\t\t\t\t\t, t2.initial_date_scheduled asc\n" +
                "\t\t\t\t\t\t\t\t\t\t\t, t2.date_predicted asc)\n" +
                "\t\t\t\t) as grp\n" +
                "\t\t\t\t, p3.id\n" +
                "\t\t\t\t, p3.user_id \n" +
                "\t\t\t\t, p3.seasonal_contest_id \n" +
                "\t\t\t\t, un.nickname \n" +
                "\t\t\t\t, (case\n" +
                "\t\t\t\t\twhen vs.count_lost = 1 and vs.count_in_contest = 1 then 'lost'\n" +
                "\t\t\t\t\twhen vs.count_void = 1 and vs.count_in_contest = 1 then 'void'\n" +
                "\t\t\t\t\twhen p3.result = 'void-won' then 'won'\n" +
                "\t\t\t\t\twhen p3.result = 'void-lost' then 'lost'\n" +
                "\t\t\t\t\telse p3.`result` \n" +
                "\t\t\t\t\tend\n" +
                "\t\t\t\t) as result\n" +
                "\t\t\t\t, p3.user_pick_value\n" +
                "\t\t\t\t, t2.initial_date_scheduled\n" +
                "\t\t\t\t, t2.date_predicted\n" +
                "\t\t\tfrom prediction p3\n" +
                "\t\t\t\tjoin (\n" +
                "\t\t\t\t\tselect \n" +
                "\t\t\t\t\t\tt1.id\n" +
                "\t\t\t\t\t\t, min(t1.date_scheduled) as initial_date_scheduled\n" +
                "\t\t\t\t\t\t, t1.date_predicted\n" +
                "\t\t\t\t\tfrom (\n" +
                "\t\t\t\t\t\tselect \n" +
                "\t\t\t\t\t\t\tp.id \n" +
                "\t\t\t\t\t\t\t, p.date_scheduled\n" +
                "\t\t\t\t\t\t\t, p.date_predicted \n" +
                "\t\t\t\t\t\tfrom prediction p\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\tselect \n" +
                "\t\t\t\t\t\t\tpsc.prediction_id\n" +
                "\t\t\t\t\t\t\t, psc.previous_date_scheduled\n" +
                "\t\t\t\t\t\t\t, p2.date_predicted \n" +
                "\t\t\t\t\t\tfrom prediction_schedule_changes psc\n" +
                "\t\t\t\t\t\t\tjoin prediction p2 on psc.prediction_id = p2.id \n" +
                "\t\t\t\t\t\t) t1\n" +
                "\t\t\t\t\tgroup by \n" +
                "\t\t\t\t\t\tt1.id\n" +
                "\t\t\t\t\t\t, t1.date_predicted\n" +
                "\t\t\t\t) t2 on p3.id = t2.id\n" +
                "\t\t\t\tjoin contest c on c.id = p3.seasonal_contest_id\n" +
                "\t\t\t\tjoin user_nickname un on un.user_id = p3.user_id\n" +
                "\t\t\t\tjoin validity_statuses vs on vs.status = p3.seasonal_validity_status\n" +
                "\t\t\twhere 1=1\n" +
                "\t\t\t\tand un.is_active = 1\n" +
                "\t\t\t\tand vs.count_in_contest = 1\n" +
                "-- \t\t\t\tand c.id = (select c.id from contest c where c.`type` = 'seasonal' and is_active = 1)\n" +
                "\t\t\t\tand c.id = '" + contestId + "'\n" +
                "\t\t\torder by \n" +
                "\t\t\t\tp3.user_id \n" +
                "\t\t\t\t, case when t2.initial_date_scheduled is null then 1 else 0 end\n" +
                "\t\t\t\t, t2.initial_date_scheduled asc\n" +
                "\t\t\t\t, t2.date_predicted asc\n" +
                "\t\t\t) t3\n" +
                "\t\twhere 1=1\n" +
                "\t\t\tand t3.result = 'won'\n" +
                "\t\tgroup by \n" +
                "\t\t\tt3.grp\n" +
                "\t\t\t, t3.nickname\n" +
                "\t\t\t, t3.user_id \n" +
                "\t\t\t, t3.seasonal_contest_id \n" +
                "\t\t\t, t3.result\n" +
                "\t\torder by\n" +
                "\t\t\tt3.nickname\n" +
                "\t\t\t, strick_length desc\n" +
                "\t\t\t, strick_avg_odds desc\n" +
                "\t\t) t4\n" +
                "\t) t5\n" +
                "where 1=1\n" +
                "\tand t5.strick_rank = 1\n" +
                "order by \n" +
                "\tt5.strick_length desc \n" +
                "\t, t5.strick_avg_odds desc;";

        return dbOp.getListOfHashMaps(conn, sql);
    }

    public List<HashMap<String,Object>> getContestResultsBiggestOddsToWrite(String contestId) {

        DatabaseOperations dbOp = new DatabaseOperations();

        String sql = "select \n" +
                "\tt1.nickname\n" +
                "\t, t1.user_id\n" +
                "\t, t1.contest_id\n" +
                "\t, t1.user_pick_value\n" +
                "from (\n" +
                "\tselect \n" +
                "\t\tun.nickname  \n" +
                "\t\t, p.user_id \n" +
                "\t\t, p.seasonal_contest_id as contest_id\n" +
                "\t\t, max(p.user_pick_value) as user_pick_value \n" +
                "\tfrom prediction p \n" +
                "\t\tjoin contest c on c.id = p.seasonal_contest_id \n" +
                "\t\tjoin user_nickname un on un.user_id = p.user_id \n" +
                "\t\tjoin validity_statuses vs on vs.status = p.seasonal_validity_status \n" +
                "\twhere 1=1\n" +
                "\t-- \tand p.seasonal_contest_id = (select c.id from contest c where c.`type` = 'seasonal' and is_active = 1)\n" +
                "\t\tand p.seasonal_contest_id = '" + contestId + "'\n" +
                "\t\tand un.is_active = 1\n" +
                "\t\tand p.unit_outcome > 0\n" +
                "\t\tand vs.count_in_contest = 1\n" +
                "\t\tand vs.count_lost != 1\n" +
                "\t\tand vs.count_void != 1\n" +
                "\tgroup by \n" +
                "\t\tun.nickname \n" +
                "\t\t, p.user_id \n" +
                "\t\t, p.seasonal_contest_id \n" +
                "\torder by user_pick_value desc\n" +
                ") t1 -- t1 is used because java does not recognize aliases\n" +
                ";";

        return dbOp.getListOfHashMaps(conn, sql);
    }

    public List<HashMap<String,Object>> getFirstThreePlaces(String contestId) {

        DatabaseOperations dbOp = new DatabaseOperations();
        Contest c = new Contest(conn, contestId);

        String sql = null;
        String contestType = c.getContestType();

        if (contestType.equals("seasonal") || contestType.equals("monthly")) {

            sql = "select \n" +
                    "\tcg.place\n" +
                    "\t, cg.user_id \n" +
                    "\t, cg.contest_id \n" +
                    "\t, cg.nickname \n" +
                    "from cr_general cg\n" +
                    "where 1=1\n" +
                    "\tand cg.contest_id = '" + contestId + "'\n" +
                    "\tand cg.place in (1, 2, 3);";

        } else if (contestType.equals("annual")) {

            sql = "select \n" +
                    "\tca.place \n" +
                    "\t, ca.user_id \n" +
                    "\t, ca.contest_id \n" +
                    "\t, un.nickname \n" +
                    "from cr_annual ca \n" +
                    "\tjoin user_nickname un on un.user_id = ca.user_id \n" +
                    "where 1=1\n" +
                    "\tand un.is_active = 1\n" +
                    "\tand ca.contest_id = '" + contestId + "'\n" +
                    "\tand ca.place in (1, 2, 3);";

        }

        return dbOp.getListOfHashMaps(conn, sql);

    }

    public List<HashMap<String,Object>> getContestResultsWrittenBiggestOdds(String contestId) {

        DatabaseOperations dbOp = new DatabaseOperations();

        String sql = "select \n" +
                "\tcbo.user_id \n" +
                "\t, cbo.contest_id \n" +
                "\t, cbo.nickname \n" +
                "from cr_biggest_odds cbo \n" +
                "where 1=1\n" +
                "\tand cbo.contest_id = '" + contestId + "'\n" +
                "\tand cbo.user_pick_value = (\n" +
                "\t\t\t\t\t\t\t\tselect \n" +
                "\t\t\t\t\t\t\t\t\tmax(cbo2.user_pick_value) \n" +
                "\t\t\t\t\t\t\t\tfrom cr_biggest_odds cbo2\n" +
                "\t\t\t\t\t\t\t\twhere cbo2.contest_id = '" + contestId + "'\n" +
                "\t\t\t\t\t\t\t);";

        return dbOp.getListOfHashMaps(conn, sql);
    }

    public List<HashMap<String,Object>> getContestResultsWrittenWinningStrick(String contestId) {

        DatabaseOperations dbOp = new DatabaseOperations();

        String sql = "select \n" +
                "\tt1.user_id\n" +
                "\t, t1.contest_id\n" +
                "\t, t1.nickname\n" +
                "from (\n" +
                "\tselect\n" +
                "\t\t(row_number() over (\n" +
                "\t\t\t\torder by \n" +
                "\t\t\t\t\tcws.strick_length desc\n" +
                "\t\t\t\t\t, cws.strick_avg_odds desc\n" +
                "\t\t\t\t)\n" +
                "\t\t) as place\n" +
                "\t\t,cws.user_id \n" +
                "\t\t, cws.contest_id \n" +
                "\t\t, cws.nickname \n" +
                "\tfrom cr_winning_strick cws \n" +
                "\twhere 1=1\n" +
                "\t\tand cws.contest_id = '" + contestId + "'\n" +
                "\t) t1\n" +
                "where t1.place = 1;";

        return dbOp.getListOfHashMaps(conn, sql);
    }

}
