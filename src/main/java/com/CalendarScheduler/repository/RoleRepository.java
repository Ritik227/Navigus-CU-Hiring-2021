package com.CalendarScheduler.repository;

import org.springframework.data.repository.CrudRepository;

import com.CalendarScheduler.domain.security.Role;


public interface RoleRepository extends CrudRepository<Role, Long> {
	Role findByname(String name);
}
