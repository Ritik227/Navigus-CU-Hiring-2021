package com.CalendarScheduler.serviceImpl;

import java.util.ArrayList;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CalendarScheduler.domain.User;
import com.CalendarScheduler.domain.UserSchedule;
import com.CalendarScheduler.domain.security.PasswordResetToken;
import com.CalendarScheduler.domain.security.UserRole;
import com.CalendarScheduler.repository.PasswordResetTokenRepository;
import com.CalendarScheduler.repository.RoleRepository;
import com.CalendarScheduler.repository.UserRepository;
import com.CalendarScheduler.service.UserService;

@Service
public class UserServiceImpl implements UserService{
	private static final Logger LOG=LoggerFactory.getLogger(UserService.class);
	@Autowired
	private PasswordResetTokenRepository passwordResetTokenRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Override
	public PasswordResetToken getPasswordResetToken(final String token) {
		return passwordResetTokenRepository.findByToken(token);
	}
	
	@Override
	public void createPasswordResetTokenForUser(final User user, final String token) {
		final PasswordResetToken myToken = new PasswordResetToken(token, user);
		passwordResetTokenRepository.save(myToken);
	}
	@Override
	public User findByUsername(String username) {
		return userRepository.findByUsername(username);
	}
//	@Override
//	public User findById(Long id){
//		return userRepository.findById(id).orElse(null);
//	}	
	@Override
	public User findByEmail (String email) {
		return userRepository.findByEmail(email);
	}
	
	@Override
	public User createUser(User user, Set<UserRole> userRoles) throws Exception{
		User localUser = userRepository.findByUsername(user.getUsername());
		
		if(localUser != null) {
			LOG.info("user {} already exists. Nothing will be done",user.getUsername());
		} else {
			for (UserRole ur : userRoles) {
				roleRepository.save(ur.getRole());
			}
			
			user.getUserRoles().addAll(userRoles);		
			UserSchedule userSchedule=new UserSchedule();
			userSchedule.setUser(user);
			
			user.setUserScheduleList(new ArrayList<UserSchedule>());
			localUser = userRepository.save(user);
		}
		
		return localUser;
	}
	
	@Override
	public User save(User user){
		return userRepository.save(user);
	}
	@Override
	public void updateUserSchedule(UserSchedule userSchedule, User user){
		userSchedule.setUser(user);
		user.getUserScheduleList().add(userSchedule);
		save(user);
	}
}
