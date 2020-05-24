package com.CalendarScheduler.service;

import java.util.Set;

import com.CalendarScheduler.domain.User;
import com.CalendarScheduler.domain.UserSchedule;
import com.CalendarScheduler.domain.security.PasswordResetToken;
import com.CalendarScheduler.domain.security.UserRole;


public interface UserService {
	PasswordResetToken getPasswordResetToken(final String token);
	
	void createPasswordResetTokenForUser(final User user, final String token);

	User findByUsername(String username);
	
	User findByEmail(String email);
	
	User createUser(User user,Set<UserRole>userRoles)throws Exception;
	void updateUserSchedule(UserSchedule userSchedule,User user);
	User save(User user);
}