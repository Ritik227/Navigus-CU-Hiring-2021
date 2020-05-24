package com.CalendarScheduler.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CalendarScheduler.domain.UserSchedule;
import com.CalendarScheduler.repository.UserScheduleRepository;
import com.CalendarScheduler.service.UserScheduleService;

@Service
public class UserScheduleServiceImpl implements UserScheduleService{
	@Autowired
	private UserScheduleRepository userScheduleRepository;
	
	@Override
	public List<UserSchedule> findAll() {
		return (List<UserSchedule>)userScheduleRepository.findAll();
	}
	@Override
	public UserSchedule findById(Long id) {
		return userScheduleRepository.findById(id).orElse(null);
	}

	@Override
	public void removeById(Long id) {
		userScheduleRepository.deleteById(id);
	}

}
