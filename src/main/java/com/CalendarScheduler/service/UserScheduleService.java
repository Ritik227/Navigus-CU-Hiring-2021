package com.CalendarScheduler.service;

import java.util.List;

import com.CalendarScheduler.domain.UserSchedule;

public interface UserScheduleService {
	List<UserSchedule> findAll();
	
	UserSchedule findById(Long id);
	
	void removeById(Long id);
}
