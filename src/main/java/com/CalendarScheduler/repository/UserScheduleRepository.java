package com.CalendarScheduler.repository;

import org.springframework.data.repository.CrudRepository;

import com.CalendarScheduler.domain.UserSchedule;

public interface UserScheduleRepository extends CrudRepository<UserSchedule, Long>{

}
