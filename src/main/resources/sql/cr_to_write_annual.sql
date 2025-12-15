select
	t13.place
	, (
		select -- get annual contest_id per user
			c.id as annual_contest_id
		from contest c
			join annual_x_seasonal_contest axsc on axsc.annual_contest_id = c.id
			join cr_general cg on cg.contest_id = axsc.seasonal_contest_id
			join contest c2 on c2.id = cg.contest_id
		where 1=1
			and c.id = :contest_id -- annual contest_id
			and cg.user_id = t13.user_id
		group by cg.user_id
	) as contest_id
	, t13.user_id
	, t13.nickname
	, t13.sum_annual_points
	, t13.best_place
	, t13.best_place_count
	, t13.second_best_place
	, t13.second_best_place_count
	, t13.third_best_place
	, t13.avg_roi
from (
	select -- sort users according to rules
		(
			row_number() over(
				order by
					t12.points desc
					, t12.best_place asc
					, t12.best_place_count desc
					, t12.second_best_place asc
					, t12.second_best_place_count desc
					, t12.third_best_place asc
					, t12.avg_roi desc
				)
		) as place
		, t12.user_id
		, t12.username as nickname
		, t12.points as sum_annual_points
		, t12.best_place
		, t12.best_place_count
		, t12.second_best_place
		, t12.second_best_place_count
		, t12.third_best_place
		, t12.avg_roi
	from (
		select -- get sum of annual points and avg_roi
			t11.user_id
			, t11.username
			, sum(cg6.annual_points) as points
			, t11.best_place
			, t11.best_place_count
			, t11.second_best_place
			, t11.second_best_place_count
			, t11.third_best_place
			, round(avg(cg6.roi), 2) as avg_roi
		from
			cr_general cg6
		join (
			select -- get third_best_place including users who don't have it
				t10.user_id
				, t10.username
				, t10.best_place
				, t10.best_place_count
				, t10.second_best_place
				, t10.second_best_place_count
				, t9.third_best_place
			from (
				select -- get third_best_place for users who have it
					t8.user_id
					, t8.username
					, t8.best_place
					, t8.best_place_count
					, t8.second_best_place
					, t8.second_best_place_count
					, min(cg5.place) as third_best_place
				from cr_general cg5
					join (
						select -- get count of second best places including users who don't have it
							t7.user_id
							, t7.username
							, t7.best_place
							, t7.best_place_count
							, t7.second_best_place
							, t6.second_best_place_count
						from (
							select -- get count of second best places for users who have it
								t5.user_id
								, t5.username
								, t5.best_place
								, t5.best_place_count
								, t5.second_best_place
								, count(cg4.place) as second_best_place_count
							from cr_general cg4
								right join (
									select -- get second_best_place for including users who don't have it
										t4.user_id
										, t4.username
										, t4.best_place
										, t4.best_place_count
										, t3.second_best_place
									from (
										select -- get second_best_place for users who have it
											t2.user_id
											, t2.username
											, t2.best_place
											, t2.best_place_count
											, min(cg3.place) as second_best_place
										from cr_general cg3
											join (
												select -- get count of best places per user
													cg2.user_id
													, u.username
													, t1.best_place
													, count(cg2.id) best_place_count
												from cr_general cg2
													join (
														select -- get best_place per user
															cg.user_id
															, min(cg.place) as best_place
														from cr_general cg
														where 1=1
															and cg.contest_id in :seas_ids
														group by cg.user_id
													) t1 on t1.user_id = cg2.user_id
													join user u on u.id = cg2.user_id
												where 1=1
													and cg2.place = t1.best_place
													and cg2.contest_id in :seas_ids
												group by cg2.user_id
											) t2 on t2.user_id = cg3.user_id
										where 1=1
											and cg3.contest_id in :seas_ids
											and cg3.place > t2.best_place
										group by
											t2.user_id
											, t2.username
									) t3
										right join ( -- joining to still get users who don't have second_best_place
											select -- get count of best places per user
												cg2.user_id
												, u.username
												, t1.best_place
												, count(cg2.id) as best_place_count
											from cr_general cg2
												join (
													select -- get best place per user
														cg.user_id
														, min(cg.place) as best_place
													from cr_general cg
													where 1=1
														and cg.contest_id in :seas_ids
													group by cg.user_id
												) t1 on t1.user_id = cg2.user_id
												join user u on u.id = cg2.user_id
											where 1=1
												and cg2.place = t1.best_place
												and cg2.contest_id in :seas_ids
											group by cg2.user_id
										) t4 on t4.user_id = t3.user_id
									) t5 on t5.user_id = cg4.user_id
							where 1=1
								and cg4.contest_id in :seas_ids
								and cg4.place = t5.second_best_place
							group by t5.user_id
						) t6
							right join (
									select -- get second_best_place for including users who don't have it
										t4.user_id
										, t4.username
										, t4.best_place
										, t4.best_place_count
										, t3.second_best_place
									from (
										select -- get second_best_place for users who have it
											t2.user_id
											, t2.username
											, t2.best_place
											, t2.best_place_count
											, min(cg3.place) as second_best_place
										from cr_general cg3
											join (
												select -- get count of best places per user
													cg2.user_id
													, u.username
													, t1.best_place
													, count(cg2.id) best_place_count
												from cr_general cg2
													join (
														select -- get best_place per user
															cg.user_id
															, min(cg.place) as best_place
														from cr_general cg
														where 1=1
															and cg.contest_id in :seas_ids
														group by cg.user_id
													) t1 on t1.user_id = cg2.user_id
													join user u on u.id = cg2.user_id
												where 1=1
													and cg2.place = t1.best_place
													and cg2.contest_id in :seas_ids
												group by cg2.user_id
											) t2 on t2.user_id = cg3.user_id
										where 1=1
											and cg3.contest_id in :seas_ids
											and cg3.place > t2.best_place
										group by t2.user_id
									) t3
										right join ( -- joining to still get users who don't have second_best_place
											select -- get count of best places per user
												cg2.user_id
												, u.username
												, t1.best_place
												, count(cg2.id) as best_place_count
											from cr_general cg2
												join (
													select -- get best place per user
														cg.user_id
														, min(cg.place) as best_place
													from cr_general cg
													where 1=1
														and cg.contest_id in :seas_ids
													group by cg.user_id
												) t1 on t1.user_id = cg2.user_id
												join user u on u.id = cg2.user_id
											where 1=1
												and cg2.place = t1.best_place
												and cg2.contest_id in :seas_ids
											group by cg2.user_id
										) t4 on t4.user_id = t3.user_id
							) t7 on t7.user_id = t6.user_id
						) t8 on t8.user_id = cg5.user_id
				where 1=1
					and cg5.contest_id in :seas_ids
					and cg5.place > t8.second_best_place
				group by cg5.user_id
			) t9
				right join (
						select -- get count of second best places including users who don't have it
							t7.user_id
							, t7.username
							, t7.best_place
							, t7.best_place_count
							, t7.second_best_place
							, t6.second_best_place_count
						from (
							select -- get count of second best places for users who have it
								t5.user_id
								, t5.username
								, t5.best_place
								, t5.best_place_count
								, t5.second_best_place
								, count(cg4.place) as second_best_place_count
							from cr_general cg4
								right join (
									select -- get second_best_place for including users who don't have it
										t4.user_id
										, t4.username
										, t4.best_place
										, t4.best_place_count
										, t3.second_best_place
									from (
										select -- get second_best_place for users who have it
											t2.user_id
											, t2.username
											, t2.best_place
											, t2.best_place_count
											, min(cg3.place) as second_best_place
										from cr_general cg3
											join (
												select -- get count of best places per user
													cg2.user_id
													, u.username
													, t1.best_place
													, count(cg2.id) best_place_count
												from cr_general cg2
													join (
														select -- get best_place per user
															cg.user_id
															, min(cg.place) as best_place
														from cr_general cg
														where 1=1
															and cg.contest_id in :seas_ids
														group by cg.user_id
													) t1 on t1.user_id = cg2.user_id
													join user u on u.id = cg2.user_id
												where 1=1
													and cg2.place = t1.best_place
													and cg2.contest_id in :seas_ids
												group by cg2.user_id
											) t2 on t2.user_id = cg3.user_id
										where 1=1
											and cg3.contest_id in :seas_ids
											and cg3.place > t2.best_place
										group by t2.user_id
									) t3
										right join ( -- joining to still get users who don't have second_best_place
											select -- get count of best places per user
												cg2.user_id
												, u.username
												, t1.best_place
												, count(cg2.id) as best_place_count
											from cr_general cg2
												join (
													select -- get best place per user
														cg.user_id
														, min(cg.place) as best_place
													from cr_general cg
													where 1=1
														and cg.contest_id in :seas_ids
													group by cg.user_id
												) t1 on t1.user_id = cg2.user_id
												join user u on u.id = cg2.user_id
											where 1=1
												and cg2.place = t1.best_place
												and cg2.contest_id in :seas_ids
											group by cg2.user_id
										) t4 on t4.user_id = t3.user_id
									) t5 on t5.user_id = cg4.user_id
							where 1=1
								and cg4.contest_id in :seas_ids
								and cg4.place = t5.second_best_place
							group by t5.user_id
						) t6
							right join (
									select -- get second_best_place for including users who don't have it
										t4.user_id
										, t4.username
										, t4.best_place
										, t4.best_place_count
										, t3.second_best_place
									from (
										select -- get second_best_place for users who have it
											t2.user_id
											, t2.username
											, t2.best_place
											, t2.best_place_count
											, min(cg3.place) as second_best_place
										from cr_general cg3
											join (
												select -- get count of best places per user
													cg2.user_id
													, u.username
													, t1.best_place
													, count(cg2.id) best_place_count
												from cr_general cg2
													join (
														select -- get best_place per user
															cg.user_id
															, min(cg.place) as best_place
														from cr_general cg
														where 1=1
															and cg.contest_id in :seas_ids
														group by cg.user_id
													) t1 on t1.user_id = cg2.user_id
													join user u on u.id = cg2.user_id
												where 1=1
													and cg2.place = t1.best_place
													and cg2.contest_id in :seas_ids
												group by cg2.user_id
											) t2 on t2.user_id = cg3.user_id
										where 1=1
											and cg3.contest_id in :seas_ids
											and cg3.place > t2.best_place
										group by t2.user_id
									) t3
										right join ( -- joining to still get users who don't have second_best_place
											select -- get count of best places per user
												cg2.user_id
												, u.username
												, t1.best_place
												, count(cg2.id) as best_place_count
											from cr_general cg2
												join (
													select -- get best place per user
														cg.user_id
														, min(cg.place) as best_place
													from cr_general cg
													where 1=1
														and cg.contest_id in :seas_ids
													group by cg.user_id
												) t1 on t1.user_id = cg2.user_id
												join user u on u.id = cg2.user_id
											where 1=1
												and cg2.place = t1.best_place
												and cg2.contest_id in :seas_ids
											group by cg2.user_id
										) t4 on t4.user_id = t3.user_id
							) t7 on t7.user_id = t6.user_id
				) t10 on t10.user_id = t9.user_id
			) t11 on t11.user_id = cg6.user_id
		where 1=1
			and cg6.contest_id in :seas_ids
		group by t11.user_id
	) t12
) t13;