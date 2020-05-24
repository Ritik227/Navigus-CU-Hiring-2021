package com.CalendarScheduler.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.CalendarScheduler.domain.User;
import com.CalendarScheduler.domain.UserSchedule;
import com.CalendarScheduler.domain.security.PasswordResetToken;
import com.CalendarScheduler.domain.security.Role;
import com.CalendarScheduler.domain.security.UserRole;
import com.CalendarScheduler.service.UserScheduleService;
import com.CalendarScheduler.service.UserService;
import com.CalendarScheduler.serviceImpl.UserSecurityService;
import com.CalendarScheduler.utility.MailConstructor;
import com.CalendarScheduler.utility.SecurityUtility;

//this class is the controller bean in the spring container
@Controller
public class HomeController {
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private MailConstructor mailConstructor;
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserSecurityService userSecurityService;
	
	@Autowired
	private UserScheduleService userScheduleService;
	
	@RequestMapping("/")
	public String index() {// returns a template name as string
		return "home";
	}

	@RequestMapping("/myAccount")
	public String myAccount() {
		return "myAccount";
	}
    @RequestMapping("/login")
    public String login(Model model) {
    	model.addAttribute("classActiveLogin", true);
		return "myAccount";
    }
    @RequestMapping("/forgetPassword")
    public String forgetPassword(Model model) {
    	model.addAttribute("classActiveForgetPassword", true);
		return "myAccount";
    }
    @RequestMapping("/newUser")
	public String newUser(Locale locale, @RequestParam("token") String token, Model model) {
		PasswordResetToken passToken = userService.getPasswordResetToken(token);
		if (passToken == null) {
			String message = "Invalid Token.";
			model.addAttribute("message", message);
			//will generate a bad request in case some malicious user attacks 
			return "redirect:/badRequest";
		}
//if the token can be found then we are going to retrieve the user using token 
		User user = passToken.getUser();
		String username = user.getUsername();
  
//below code does that the current login session will be only for this user whose name was retrieved through passToken.getUser()
		UserDetails userDetails = userSecurityService.loadUserByUsername(username);
//creates a authentication environment based on user name and password
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
				userDetails.getAuthorities());
		//sets authentication for the present user
		SecurityContextHolder.getContext().setAuthentication(authentication);

		model.addAttribute("user",user);
		model.addAttribute("classActiveEdit", true);

		return "myAccount";//template referring to the page that will be seen after all validation and verification is done
  }
    @RequestMapping(value="/newUser", method = RequestMethod.POST)
	public String newUserPost(
			HttpServletRequest request,
			@ModelAttribute("email") String userEmail,
			@ModelAttribute("username") String username,
			Model model
			) throws Exception{
		model.addAttribute("classActiveNewAccount", true);
		model.addAttribute("email", userEmail);
		model.addAttribute("username", username);
		
		if (userService.findByUsername(username) != null) {
			model.addAttribute("usernameExists", true);	
			return "myAccount";
		}
		
		if (userService.findByEmail(userEmail) != null) {
			model.addAttribute("emailExists", true);
			return "myAccount";
		}
		
		User user = new User();
		user.setUsername(username);
		user.setEmail(userEmail);
		
		String password = SecurityUtility.randomPassword();
		
		String encryptedPassword = SecurityUtility.passwordEncoder().encode(password);
		user.setPassword(encryptedPassword);
		
		Role role = new Role();
		role.setRoleId(1);
		role.setName("ROLE_USER");
		Set<UserRole> userRoles = new HashSet<>();
		userRoles.add(new UserRole(user, role));
		userService.createUser(user, userRoles);
		
		String token = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(user, token);
		
		String appUrl = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
		
		SimpleMailMessage email = mailConstructor.constructResetTokenEmail(appUrl, request.getLocale(), token, user, password);
		
		mailSender.send(email);
		
		model.addAttribute("emailSent", "true");
		
		return "myAccount";
	}
    @RequestMapping(value="/addNewSchedule", method=RequestMethod.GET)
	public String addNewSchedule(
			Model model, Principal principal
			){
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		
		UserSchedule userSchedule = new UserSchedule();
		model.addAttribute("userSchedule", userSchedule);
		
		return "addSchedule";
	}
 
	@RequestMapping(value="/addNewSchedule", method=RequestMethod.POST)
	public String addNewShippingAddressPost(
			@ModelAttribute("userSchedule") UserSchedule userSchedule,
			Principal principal, Model model
			){
		User user = userService.findByUsername(principal.getName());
		userService.updateUserSchedule(userSchedule, user);
		
		model.addAttribute("user", user);
		model.addAttribute("userScheduleList", user.getUserScheduleList());
		
		return "redirect:eventList";
	}
	@RequestMapping("/eventList")
	public String bookList(Model model) {
		List<UserSchedule> eventList = userScheduleService.findAll();
		model.addAttribute("eventList",eventList);
		return "eventList";
		
	}
	@RequestMapping("/updateEvent")
	public String updateEvent(
			@ModelAttribute("id") Long scheduleId, Principal principal, Model model
			) {
		User user = userService.findByUsername(principal.getName());
		UserSchedule userSchedule= userScheduleService.findById(scheduleId);
		
		if(user.getId() != userSchedule.getUser().getId()) {
			return "badRequestPage";
		} else {
			model.addAttribute("user", user);
			
			model.addAttribute("userSchedule", userSchedule);
			
			model.addAttribute("userShippingList", user.getUserScheduleList());
			return "userInfo";
		}
	}

	@RequestMapping("/removeUserSchedule")
	public String removeUserSchedule(
			@ModelAttribute("id") Long userScheduleId, Principal principal, Model model
			){
		User user = userService.findByUsername(principal.getName());
		UserSchedule userSchedule = userScheduleService.findById(userScheduleId);
			model.addAttribute("user", user);	
			userScheduleService.removeById(userScheduleId);
			List<UserSchedule> eventList = userScheduleService.findAll();
			model.addAttribute("eventList",eventList);
			return "eventList";
	}
	@RequestMapping("/userInfo")
	public String userInfo(@RequestParam("id") Long id, Model model) {
		UserSchedule userSchedule = userScheduleService.findById(id);
		model.addAttribute("userSchedule", userSchedule);
		return "userInfo";
	}
}