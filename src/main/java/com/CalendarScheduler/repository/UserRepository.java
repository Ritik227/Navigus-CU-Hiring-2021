package com.CalendarScheduler.repository;

import org.springframework.data.repository.CrudRepository;

import com.CalendarScheduler.domain.User;


public interface UserRepository extends CrudRepository<User, Long> {
	User findByUsername(String username);
	
	User findByEmail(String email);
}
