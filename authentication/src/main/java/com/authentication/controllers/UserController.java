package com.authentication.controllers;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.authentication.models.User;
import com.authentication.repositories.UserRepository;
import com.authentication.services.UserService;

@Controller
public class UserController {
	
	@Autowired
	private UserService service;
	
	@Autowired
	private UserRepository repository;
	
	@RequestMapping("/")
	public String welcome(HttpSession session) {
		
		User user = this.service.findCurrentUser(session);
		if (user == null)
			return "redirect:/login";
		return "landing.jsp";
	}
	
	@RequestMapping("/login")
	public String loginPage(@ModelAttribute("newLogin") User user) {
		return "login.jsp";
	}
	
	@PostMapping("/login")
	public String login(@Valid @ModelAttribute("newLogin") User user, BindingResult result, HttpSession session, Model model) {
		
		user = this.service.authenticate(user);
		if(user != null) {
			session.setAttribute("userID", user.getId());
			return "redirect:/";
		}
		
		model.addAttribute("message", "Invalid Credentials.");
			
		return "login.jsp";
	}
	
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.removeAttribute("userID");
		return "redirect:/login";
	}
	
	@GetMapping("/register")
	public String registerPage(@ModelAttribute("newUser") User user) {
		return "register.jsp";
	}
	
	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("newUser") User user, BindingResult result) {
		
		if(!user.getPassword().equals(user.getConfirm())) {
			result.rejectValue("confirm", "Matches", "The passwords must match!!!");
		}
		
		if(this.repository.findByEmail(user.getEmail()).isPresent()) {
			result.rejectValue("email", "Matches", "The email address is unavailable.");
		}
		
		if(result.hasErrors())
			return "logAndReg.jsp";
		
		this.service.create(user);
		return "redirect:/";
	}
}
