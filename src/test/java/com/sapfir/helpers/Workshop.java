package com.sapfir.helpers;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Workshop {
    public static void main(String[] args) {

        final Logger Log = LogManager.getLogger(Workshop.class.getName());

        DatabaseOperations dbOp = new DatabaseOperations();
        Connection conn = dbOp.connectToDatabase();

        DateTimeOperations dtOp = new DateTimeOperations();
        PredictionOperations predOp = new PredictionOperations(conn);
        Contest contest = new Contest(conn, "2deb734e-ce85-11e8-8022-74852a015562");

       String sql = "-- roi in contest by users\n" +
               "select \n" +
               "\tt3.nickname\n" +
               "\t, sum(case when t3.result = 'not-played' then 0 else 1 end) as bets\n" +
               "\t, count(distinct date(t3.kiev_date_predicted)) as act_days\n" +
               "\t, round(sum(\n" +
               "\t\tcase \n" +
               "\t\t\twhen t3.count_lost = 1 then '0'\n" +
               "\t\t\twhen t3.count_void = 1 then '1'\n" +
               "\t\t\twhen t3.result = 'void' then '1'\n" +
               "\t\t\twhen t3.result = 'won' then t3.user_pick_value\n" +
               "\t\t\twhen t3.result = 'void-won' then t3.user_pick_value\t\t\t\n" +
               "\t\tend \n" +
               "\t), 2) as won\n" +
               "\t, sum(case when t3.unit_outcome < 0 then t3.unit_outcome end) * -1 as lost\n" +
               "\t, round(sum(t3.unit_outcome), 2) as units\n" +
               "\t, round(((sum(t3.unit_outcome) / sum(case when t3.result = 'not-played' then 0 else 1 end)) * 100), 1) as roi\n" +
               "from (\n" +
               "\tselect \n" +
               "\t\tun.nickname \n" +
               "\t\t, p3.id \n" +
               "\t\t, p3.monthly_contest_id \n" +
               "\t\t, p3.seasonal_validity_status_overruled \n" +
               "\t\t, p3.seasonal_validity_status \n" +
               "\t\t, p3.monthly_validity_status_overruled \n" +
               "\t\t, p3.monthly_validity_status \n" +
               "\t\t, vs.count_lost \n" +
               "\t\t, vs.count_void \n" +
               "\t\t, convert_tz(t2.initial_date_scheduled, 'UTC', 'Europe/Kiev') as kiev_init_date\n" +
               "\t-- \t, t2.initial_date_scheduled\n" +
               "\t\t, convert_tz(t2.date_predicted, 'UTC', 'Europe/Kiev') as kiev_date_predicted\n" +
               "\t-- \t, t2.date_predicted\n" +
               "\t\t, p3.competitors \n" +
               "\t\t, p3.user_pick_value \n" +
               "\t\t, p3.main_score \n" +
               "\t\t, p3.`result`\n" +
               "\t\t, (\n" +
               "\t\t\tcase \n" +
               "\t\t\t\twhen vs.count_lost = 1 then '-1'\n" +
               "\t\t\t\twhen vs.count_void = 1 then '0'\n" +
               "\t\t\t\telse p3.unit_outcome \n" +
               "\t\t\tend\n" +
               "\t\t) as unit_outcome\n" +
               "\tfrom (\n" +
               "\t\tselect \n" +
               "\t\t\tt1.id\n" +
               "\t\t\t, min(t1.date_scheduled) as initial_date_scheduled\n" +
               "\t\t\t, t1.date_predicted\n" +
               "\t\tfrom (\n" +
               "\t\t\tselect \n" +
               "\t\t\t\tp.id \n" +
               "\t\t\t\t, p.date_scheduled\n" +
               "\t\t\t\t, p.date_predicted \n" +
               "\t\t\tfrom prediction p\n" +
               "\t\t\t\n" +
               "\t\t\tunion all -- to combine date_scheduled and previous_date_scheduled\n" +
               "\t\t\t\n" +
               "\t\t\tselect \n" +
               "\t\t\t\tpsc.prediction_id\n" +
               "\t\t\t\t, psc.previous_date_scheduled \n" +
               "\t\t\t\t, p2.date_predicted \n" +
               "\t\t\tfrom prediction_schedule_changes psc\n" +
               "\t\t\t\tjoin prediction p2 on p2.id = psc.prediction_id \n" +
               "\t\t\t) t1\n" +
               "\t\twhere 1=1\n" +
               "\t\tgroup by \n" +
               "\t\t\tt1.id\n" +
               "\t\t\t, t1.date_predicted\n" +
               "\t\t) t2\n" +
               "\t\tjoin prediction p3 on p3.id = t2.id\n" +
               "\t\tjoin user u on u.id = p3.user_id \n" +
               "\t\tjoin user_nickname un on un.user_id = u.id \n" +
               "\t\tjoin validity_statuses vs on vs.status = p3.seasonal_validity_status  \n" +
               "\twhere 1=1\n" +
               "\t\tand un.is_active = 1\n" +
               "\t\tand p3.seasonal_contest_id = (select c.id from contest c where c.`type` = 'seasonal' and is_active = 1)\n" +
               "-- \t\tand p3.seasonal_contest_id = 'bd2ac2ef-68b6-11ea-a24f-74852a015562'\n" +
               "-- \t\tand p3.monthly_contest_id = '5b6f16c9-29ea-11ea-9120-74852a015562'\n" +
               " \t\tand vs.count_in_contest = 1\n" +
               "\torder by \n" +
               "\t\tun.nickname \n" +
               "\t\t, case when t2.initial_date_scheduled is null then 1 else 0 end\n" +
               "\t\t, t2.initial_date_scheduled asc\n" +
               "\t\t, t2.date_predicted asc\n" +
               "\t) t3\n" +
               "group by \n" +
               "\tt3.nickname\n" +
               "order by \n" +
               "\tunits desc\n" +
               "\t, t3.nickname;";

        ExecuteQuery eq = new ExecuteQuery(conn, sql);
        ResultSet rs = eq.getSelectResult();
        List<HashMap<String,Object>> list = new ArrayList<>();

        try {
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();

            while (rs.next()) {
                HashMap<String,Object> row = new HashMap<String, Object>(columns);
                for(int i=1; i<=columns; ++i) {
                    row.put(md.getColumnName(i),rs.getObject(i));
                }
                list.add(row);
            }
        } catch (SQLException ex) {
            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        }
        eq.cleanUp();

        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).get("nickname"));
        }


        System.out.println("stop here");

        dbOp.closeConnection(conn);
    }
}
