select
	row_number() over (order by t5.units desc) as place
	, t5.nickname
	, t5.user_id
	, t5.seasonal_contest_id as contest_id
	, t5.final_bets_count
	, t5.orig_bets_count
	, t5.active_days
	, t5.won
	, t5.lost
	, t5.units
	, t5.roi
from (
	select
		t4.nickname
		, t4.user_id
		, t4.seasonal_contest_id
		, (case when t4.bets < 100 then 100 else t4.bets end) as final_bets_count
		, t4.bets as orig_bets_count
		, t4.active_days
		, t4.won
		, (case when t4.bets < 100 then (t4.lost + (100 - t4.bets)) else t4.lost end) as lost
		, (case when t4.bets < 100 then (t4.units - (100 - t4.bets)) else t4.units end) as units
		, cast((case when t4.bets < 100 then (t4.units - (100 - t4.bets)) else t4.units end) as decimal(5,2)) as roi
	from (
		select
			t3.nickname
			, t3.user_id
			, t3.seasonal_contest_id
			, sum(case when t3.result = 'not-played' then 0 else 1 end) as bets
			, count(distinct date(t3.kiev_date_predicted)) as active_days
			, cast(sum(
				case
					when t3.count_lost = 1 then '0'
					when t3.count_void = 1 then '1'
					when t3.result = 'void' then '1'
					when t3.result = 'won' then t3.user_pick_value
					when t3.result = 'void-won' then t3.user_pick_value * 0.5 + 0.5
					when t3.result = 'void-lost' then '0.5'
				end
			) as decimal(5,2)) as won
			, sum(case when t3.unit_outcome <= 0 then t3.unit_outcome end) * -1 as lost
			, cast(sum(t3.unit_outcome) as decimal(5,2)) as units
			, cast((sum(t3.unit_outcome) / sum(case when t3.result = 'not-played' then 0 else 1 end)) * 100 as decimal(5,2)) as roi
		from (
			select
				un.nickname
				, p3.user_id
				, p3.seasonal_contest_id
				, p3.seasonal_validity_status as seas_st
				, p3.seasonal_validity_status_overruled as seas_st_over
				, p3.monthly_validity_status as mon_st
				, p3.monthly_validity_status_overruled as mon_st_over
				, vs.count_lost
				, vs.count_void
				, convert_tz(t2.date_predicted, 'UTC', 'Europe/Kiev') as kiev_date_predicted
				, p3.user_pick_value
				, p3.`result`
				, (
					case
						when vs.count_lost = 1 then '-1'
						when vs.count_void = 1 then '0'
						else p3.unit_outcome
					end
				) as unit_outcome
			from (
				select
					t1.id
					, min(t1.date_scheduled) as initial_date_scheduled
					, t1.date_predicted
				from (
					select
						p.id
						, p.date_scheduled
						, p.date_predicted
					from prediction p

					union all -- to combine date_scheduled and previous_date_scheduled

					select
						psc.prediction_id
						, psc.previous_date_scheduled
						, p2.date_predicted
					from prediction_schedule_changes psc
						join prediction p2 on p2.id = psc.prediction_id
					) t1 -- finding all date_scheduled, including postponed
				where 1=1
				group by
					t1.id
					, t1.date_predicted
				) t2 -- finding initial date_scheduled per prediction
				join prediction p3 on p3.id = t2.id
				join user u on u.id = p3.user_id
				join user_nickname un on un.user_id = u.id
				join validity_statuses vs on vs.status = p3.seasonal_validity_status
			where 1=1
				and un.is_active = 1
				and p3.seasonal_contest_id = :contest_id
		 		and vs.count_in_contest = 1
			order by
				un.nickname
				, case when t2.initial_date_scheduled is null then 1 else 0 end
				, t2.initial_date_scheduled asc
				, t2.date_predicted asc
				, t2.id asc
			) t3 -- all predictions that count in contest with correct unit_outcome based on status
		group by
			t3.nickname
			, t3.user_id
			, t3.seasonal_contest_id
		) t4 -- calculating raw contest result measures
	) t5 -- applying rules for user who did no make 100 predictions
; -- ordering results by units