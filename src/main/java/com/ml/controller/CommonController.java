package com.ml.controller;

import com.ml.controller.vm.RegisterVM;
import com.ml.entity.User;
import com.ml.repository.UserRepository;
import com.ml.service.MailService;
import com.ml.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

@Controller
public class CommonController {

    private final UserService userService;

    private final UserRepository userRepository;

    private final MailService mailService;

    public CommonController(UserService userService, UserRepository userRepository, MailService mailService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity registerUser(@Valid RegisterVM registerVM) {
        HttpHeaders textPlainHeaders = new HttpHeaders();
        textPlainHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        // @formatter:off
        return userRepository.findOneByUsername(registerVM.getUsername())
                             .map(user -> new ResponseEntity("username already in use", textPlainHeaders, HttpStatus.BAD_REQUEST))
                             .orElseGet(() -> userRepository.findOneByEmail(registerVM.getEmail())
                                                            .map(user -> new ResponseEntity("email address already in use", textPlainHeaders, HttpStatus.BAD_REQUEST))
                                                            .orElseGet(() -> {
                                                                User user = userService.createUser(registerVM.getUsername(), registerVM.getPassword(), registerVM.getEmail());
                                                                mailService.sendActivationEmail(user);
                                                                return new ResponseEntity(HttpStatus.CREATED);
                                                            }));
        // @formatter:on
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activeUser(@RequestParam(value = "key") String key) {
        return userService.activateRegistration(key).map(user -> new ResponseEntity<String>(HttpStatus.OK))
                          .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
