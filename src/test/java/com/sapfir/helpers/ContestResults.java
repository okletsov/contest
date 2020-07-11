package com.sapfir.helpers;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class ContestResults {

    private final Connection conn;

    public ContestResults(Connection conn) {
        this.conn = conn;
    }

    public int getAnnualPointsByPlace(int place) {
        if (place == 1) {
            return 10;
        } else if (place == 2) {
            return 8;
        } else if (place == 3) {
            return 6;
        } else if (place == 4) {
            return 5;
        } else if (place == 5) {
            return 4;
        } else if (place == 6) {
            return 3;
        } else if (place == 7) {
            return 2;
        } else if (place == 8) {
            return 1;
        } else {
            return 0;
        }
    }

    public List<HashMap<String,Object>> getGeneralContestResultsToWrite(String contestId) {

        Contest c = new Contest(conn, contestId);
        DatabaseOperations dbOp = new DatabaseOperations();

        String contestType = c.getContestType();
        String sql;

        if (contestType.equals("seasonal")) {
            sql = "select \n" +
                    "\t(\n" +
                    "\t\tcase \n" +
                    "\t\t\twhen t5.active_days >=30 then (row_number() over (order by \n" +
                    "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tcase when t5.active_days >= 30 then 0 else 1 end\n" +
                    "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t, t5.units desc\n" +
                    "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t)\n" +
                    "\t\t\t\t\t\t\t\t\t\t)\n" +
                    "\t\telse 0 end\t\t\n" +
                    "\t) as place\n" +
                    "\t, t5.nickname\n" +
                    "\t, t5.user_id\n" +
                    "\t, t5.seasonal_contest_id as contest_id\n" +
                    "\t, t5.final_bets_count\n" +
                    "\t, t5.orig_bets_count\n" +
                    "\t, t5.active_days\n" +
                    "\t, t5.won\n" +
                    "\t, t5.lost\n" +
                    "\t, t5.units\n" +
                    "\t, t5.roi\n" +
                    "from (\n" +
                    "\tselect \n" +
                    "\t\tt4.nickname\n" +
                    "\t\t, t4.user_id\n" +
                    "\t\t, t4.seasonal_contest_id\n" +
                    "\t\t, (case when t4.bets < 100 then 100 else t4.bets end) as final_bets_count\n" +
                    "\t\t, t4.bets as orig_bets_count\n" +
                    "\t\t, t4.active_days\n" +
                    "\t\t, t4.won\n" +
                    "\t\t, (case when t4.bets < 100 then (t4.lost + (100 - t4.bets)) else t4.lost end) as lost\n" +
                    "\t\t, (case when t4.bets < 100 then (t4.units - (100 - t4.bets)) else t4.units end) as units\n" +
                    "\t\t, cast((case when t4.bets < 100 then (t4.units - (100 - t4.bets)) else t4.units end) as decimal(5,2)) as roi\n" +
                    "\tfrom (\n" +
                    "\t\tselect \n" +
                    "\t\t\tt3.nickname\n" +
                    "\t\t\t, t3.user_id\n" +
                    "\t\t\t, t3.seasonal_contest_id\n" +
                    "\t\t\t, sum(case when t3.result = 'not-played' then 0 else 1 end) as bets\n" +
                    "\t\t\t, count(distinct date(t3.kiev_date_predicted)) as active_days\n" +
                    "\t\t\t, cast(sum(\n" +
                    "\t\t\t\tcase \n" +
                    "\t\t\t\t\twhen t3.count_lost = 1 then '0'\n" +
                    "\t\t\t\t\twhen t3.count_void = 1 then '1'\n" +
                    "\t\t\t\t\twhen t3.result = 'void' then '1'\n" +
                    "\t\t\t\t\twhen t3.result = 'won' then t3.user_pick_value\n" +
                    "\t\t\t\t\twhen t3.result = 'void-won' then t3.user_pick_value\t\t\t\n" +
                    "\t\t\t\tend \n" +
                    "\t\t\t) as decimal(5,2)) as won\n" +
                    "\t\t\t, sum(case when t3.unit_outcome < 0 then t3.unit_outcome end) * -1 as lost\n" +
                    "\t\t\t, cast(sum(t3.unit_outcome) as decimal(5,2)) as units\n" +
                    "\t\t\t, cast((sum(t3.unit_outcome) / sum(case when t3.result = 'not-played' then 0 else 1 end)) * 100 as decimal(5,2)) as roi\n" +
                    "\t\tfrom (\n" +
                    "\t\t\tselect \n" +
                    "\t\t\t\tun.nickname \n" +
                    "\t\t\t\t, p3.user_id \n" +
                    "\t\t\t\t, p3.seasonal_contest_id \n" +
                    "\t\t\t\t, p3.seasonal_validity_status as seas_st\n" +
                    "\t\t\t\t, p3.seasonal_validity_status_overruled as seas_st_over\n" +
                    "\t\t\t\t, p3.monthly_validity_status as mon_st\n" +
                    "\t\t\t\t, p3.monthly_validity_status_overruled as mon_st_over\n" +
                    "\t\t\t\t, vs.count_lost \n" +
                    "\t\t\t\t, vs.count_void \n" +
                    "\t\t\t\t, convert_tz(t2.date_predicted, 'UTC', 'Europe/Kiev') as kiev_date_predicted\n" +
                    "\t\t\t\t, p3.user_pick_value \n" +
                    "\t\t\t\t, p3.`result`\n" +
                    "\t\t\t\t, (\n" +
                    "\t\t\t\t\tcase \n" +
                    "\t\t\t\t\t\twhen vs.count_lost = 1 then '-1'\n" +
                    "\t\t\t\t\t\twhen vs.count_void = 1 then '0'\n" +
                    "\t\t\t\t\t\telse p3.unit_outcome \n" +
                    "\t\t\t\t\tend\n" +
                    "\t\t\t\t) as unit_outcome\n" +
                    "\t\t\tfrom (\n" +
                    "\t\t\t\tselect \n" +
                    "\t\t\t\t\tt1.id\n" +
                    "\t\t\t\t\t, min(t1.date_scheduled) as initial_date_scheduled\n" +
                    "\t\t\t\t\t, t1.date_predicted\n" +
                    "\t\t\t\tfrom (\n" +
                    "\t\t\t\t\tselect \n" +
                    "\t\t\t\t\t\tp.id \n" +
                    "\t\t\t\t\t\t, p.date_scheduled\n" +
                    "\t\t\t\t\t\t, p.date_predicted \n" +
                    "\t\t\t\t\tfrom prediction p\n" +
                    "\t\t\t\t\t\n" +
                    "\t\t\t\t\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
                    "\t\t\t\t\t\n" +
                    "\t\t\t\t\tselect \n" +
                    "\t\t\t\t\t\tpsc.prediction_id\n" +
                    "\t\t\t\t\t\t, psc.previous_date_scheduled \n" +
                    "\t\t\t\t\t\t, p2.date_predicted \n" +
                    "\t\t\t\t\tfrom prediction_schedule_changes psc\n" +
                    "\t\t\t\t\t\tjoin prediction p2 on p2.id = psc.prediction_id \n" +
                    "\t\t\t\t\t) t1 -- finding all date_scheduled, including postponed\n" +
                    "\t\t\t\twhere 1=1\n" +
                    "\t\t\t\tgroup by \n" +
                    "\t\t\t\t\tt1.id\n" +
                    "\t\t\t\t\t, t1.date_predicted\n" +
                    "\t\t\t\t) t2 -- finding initial date_scheduled per prediction\n" +
                    "\t\t\t\tjoin prediction p3 on p3.id = t2.id\n" +
                    "\t\t\t\tjoin user u on u.id = p3.user_id \n" +
                    "\t\t\t\tjoin user_nickname un on un.user_id = u.id \n" +
                    "\t\t\t\tjoin validity_statuses vs on vs.status = p3.seasonal_validity_status  \n" +
                    "\t\t\twhere 1=1\n" +
                    "\t\t\t\tand un.is_active = 1\n" +
                    "\t\t\t\tand p3.seasonal_contest_id = '" + contestId + "'\n" +
                    "\t\t \t\tand vs.count_in_contest = 1\n" +
                    "\t\t\torder by \n" +
                    "\t\t\t\tun.nickname \n" +
                    "\t\t\t\t, case when t2.initial_date_scheduled is null then 1 else 0 end\n" +
                    "\t\t\t\t, t2.initial_date_scheduled asc\n" +
                    "\t\t\t\t, t2.date_predicted asc\n" +
                    "\t\t\t) t3 -- all predictions that count in contest with correct unit_outcome based on status \n" +
                    "\t\tgroup by \n" +
                    "\t\t\tt3.nickname\n" +
                    "\t\t\t, t3.user_id\n" +
                    "\t\t\t, t3.seasonal_contest_id\n" +
                    "\t\t) t4 -- calculating raw contest result measures\n" +
                    "\t) t5 -- applying rules for user who did no make 100 predictions\n" +
                    "; -- applying rules for users with less than 30 active days";
        } else {
            sql = "select \n" +
                    "\tt5.place\n" +
                    "\t, t5.nickname\n" +
                    "\t, t5.user_id\n" +
                    "\t, t5.contest_id\n" +
                    "\t, t5.final_bets_count\n" +
                    "\t, t5.orig_bets_count\n" +
                    "\t, t5.active_days\n" +
                    "\t, t5.won\n" +
                    "\t, t5.lost\n" +
                    "\t, t5.units\n" +
                    "\t, t5.roi\n" +
                    "from (\n" +
                    "\tselect \n" +
                    "\t\t(row_number() over (order by t4.roi desc)) as place\n" +
                    "\t\t, t4.nickname\n" +
                    "\t\t, t4.user_id\n" +
                    "\t\t, t4.contest_id\n" +
                    "\t\t, t4.bets as final_bets_count\n" +
                    "\t\t, t4.bets as orig_bets_count\n" +
                    "\t\t, t4.active_days\n" +
                    "\t\t, t4.won\n" +
                    "\t\t, t4.lost\n" +
                    "\t\t, t4.units\n" +
                    "\t\t, t4.roi\n" +
                    "\tfrom (\n" +
                    "\t\tselect \n" +
                    "\t\t\tt3.nickname\n" +
                    "\t\t\t, t3.user_id\n" +
                    "\t\t\t, t3.monthly_contest_id as contest_id\n" +
                    "\t\t\t, sum(case when t3.result = 'not-played' then 0 else 1 end) as bets\n" +
                    "\t\t\t, count(distinct date(t3.kiev_date_predicted)) as active_days\n" +
                    "\t\t\t, cast(sum(\n" +
                    "\t\t\t\tcase \n" +
                    "\t\t\t\t\twhen t3.count_lost = 1 then '0'\n" +
                    "\t\t\t\t\twhen t3.count_void = 1 then '1'\n" +
                    "\t\t\t\t\twhen t3.result = 'void' then '1'\n" +
                    "\t\t\t\t\twhen t3.result = 'won' then t3.user_pick_value\n" +
                    "\t\t\t\t\twhen t3.result = 'void-won' then t3.user_pick_value\t\t\t\n" +
                    "\t\t\t\tend \n" +
                    "\t\t\t) as decimal(5,2)) as won\n" +
                    "\t\t\t, sum(case when t3.unit_outcome < 0 then t3.unit_outcome end) * -1 as lost\n" +
                    "\t\t\t, cast(sum(t3.unit_outcome) as decimal(5,2)) as units\n" +
                    "\t\t\t, cast((sum(t3.unit_outcome) / sum(case when t3.result = 'not-played' then 0 else 1 end)) * 100 as decimal(5,2)) as roi\n" +
                    "\t\tfrom (\n" +
                    "\t\t\tselect \n" +
                    "\t\t\t\tun.nickname \n" +
                    "\t\t\t\t, p3.user_id \n" +
                    "\t\t\t\t, p3.monthly_contest_id \n" +
                    "\t\t\t\t, p3.monthly_validity_status as mon_st\n" +
                    "\t\t\t\t, p3.monthly_validity_status_overruled as mon_st_over\n" +
                    "\t\t\t\t, vs.count_lost \n" +
                    "\t\t\t\t, vs.count_void \n" +
                    "\t\t\t\t, convert_tz(t2.date_predicted, 'UTC', 'Europe/Kiev') as kiev_date_predicted\n" +
                    "\t\t\t\t, p3.user_pick_value \n" +
                    "\t\t\t\t, p3.`result`\n" +
                    "\t\t\t\t, (\n" +
                    "\t\t\t\t\tcase \n" +
                    "\t\t\t\t\t\twhen vs.count_lost = 1 then '-1'\n" +
                    "\t\t\t\t\t\twhen vs.count_void = 1 then '0'\n" +
                    "\t\t\t\t\t\telse p3.unit_outcome \n" +
                    "\t\t\t\t\tend\n" +
                    "\t\t\t\t) as unit_outcome\n" +
                    "\t\t\tfrom (\n" +
                    "\t\t\t\tselect \n" +
                    "\t\t\t\t\tt1.id\n" +
                    "\t\t\t\t\t, min(t1.date_scheduled) as initial_date_scheduled\n" +
                    "\t\t\t\t\t, t1.date_predicted\n" +
                    "\t\t\t\tfrom (\n" +
                    "\t\t\t\t\tselect \n" +
                    "\t\t\t\t\t\tp.id \n" +
                    "\t\t\t\t\t\t, p.date_scheduled\n" +
                    "\t\t\t\t\t\t, p.date_predicted \n" +
                    "\t\t\t\t\tfrom prediction p\n" +
                    "\t\t\t\t\t\n" +
                    "\t\t\t\t\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
                    "\t\t\t\t\t\n" +
                    "\t\t\t\t\tselect \n" +
                    "\t\t\t\t\t\tpsc.prediction_id\n" +
                    "\t\t\t\t\t\t, psc.previous_date_scheduled \n" +
                    "\t\t\t\t\t\t, p2.date_predicted \n" +
                    "\t\t\t\t\tfrom prediction_schedule_changes psc\n" +
                    "\t\t\t\t\t\tjoin prediction p2 on p2.id = psc.prediction_id \n" +
                    "\t\t\t\t\t) t1 -- finding all date_scheduled, including postponed\n" +
                    "\t\t\t\twhere 1=1\n" +
                    "\t\t\t\tgroup by \n" +
                    "\t\t\t\t\tt1.id\n" +
                    "\t\t\t\t\t, t1.date_predicted\n" +
                    "\t\t\t\t) t2 -- finding initial date_scheduled per prediction\n" +
                    "\t\t\t\tjoin prediction p3 on p3.id = t2.id\n" +
                    "\t\t\t\tjoin user u on u.id = p3.user_id \n" +
                    "\t\t\t\tjoin user_nickname un on un.user_id = u.id \n" +
                    "\t\t\t\tjoin validity_statuses vs on vs.status = p3.monthly_validity_status \n" +
                    "\t\t\twhere 1=1\n" +
                    "\t\t\t\tand un.is_active = 1\n" +
                    "\t\t\t\tand p3.monthly_contest_id = '" + contestId + "'\n" +
                    "\t\t \t\tand vs.count_in_contest = 1\n" +
                    "\t\t\torder by \n" +
                    "\t\t\t\tun.nickname \n" +
                    "\t\t\t\t, case when t2.initial_date_scheduled is null then 1 else 0 end\n" +
                    "\t\t\t\t, t2.initial_date_scheduled asc\n" +
                    "\t\t\t\t, t2.date_predicted asc\n" +
                    "\t\t\t) t3 -- all predictions that count in contest with correct unit_outcome based on status \n" +
                    "\t\tgroup by \n" +
                    "\t\t\tt3.nickname\n" +
                    "\t\t\t, t3.user_id\n" +
                    "\t\t\t, t3.monthly_contest_id\n" +
                    "\t\t) t4\n" +
                    "\twhere 1=1\n" +
                    "\t\tand t4.bets >= 30\n" +
                    "\t) t5;";
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
                "\t\t\t\t\trow_number () over (partition by \n" +
                "\t\t\t\t\t\t\t\t\t\t\tp3.user_id\n" +
                "\t\t\t\t\t\t\t\t\t\t\t, p3.result \n" +
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
                "\t\t\t\t, p3.result\n" +
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
                "\t, t5.strick_avg_odds desc\n" +
                ";";

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

        String sql = "select \n" +
                "\tcg.place\n" +
                "\t, cg.user_id \n" +
                "\t, cg.contest_id \n" +
                "\t, cg.nickname \n" +
                "from cr_general cg\n" +
                "where 1=1\n" +
                "\tand cg.contest_id = '" + contestId + "'\n" +
                "\tand cg.place in (1, 2, 3);";

        return dbOp.getListOfHashMaps(conn, sql);

    }

}
