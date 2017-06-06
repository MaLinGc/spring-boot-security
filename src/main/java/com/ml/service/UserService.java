package com.ml.service;

import com.ml.entity.Role;
import com.ml.entity.User;
import com.ml.entity.UserRole;
import com.ml.entity.UserRoleId;
import com.ml.repository.RoleRepository;
import com.ml.repository.UserRepository;
import com.ml.repository.UserRoleRepository;
import com.ml.security.AuthoritiesConstants;
import com.ml.security.CustomUserDetails;
import com.ml.util.RandomUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final UserRoleRepository userRoleRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public CustomUserDetails findUserDetailsByUsername(String username) {
        if (StringUtils.isBlank(username))
            return null;
        Optional<User> userOptional = userRepository.findOneByUsername(username);
        if (!userOptional.isPresent())
            return null;
        User user = userOptional.get();
        CustomUserDetails securityUser = new CustomUserDetails(user);
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        Set<Role> roleSet = userRoles.stream().map(UserRole::getRole).collect(Collectors.toSet());
        securityUser.setRoleSet(roleSet);
        return securityUser;
    }

    public User createUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setName(username);
        String encryptedPassword = passwordEncoder.encode(password);
        user.setPassword(encryptedPassword);
        user.setEmail(email);
        user.setActivated(false);
        user.setActivationKey(RandomUtil.generateActivationKey());
        userRepository.save(user);

        roleRepository.findOneByName(AuthoritiesConstants.USER).map(role -> {
            UserRoleId userRoleId = new UserRoleId(user.getId(), role.getId());
            UserRole userRole = new UserRole();
            userRole.setId(userRoleId);
            userRoleRepository.save(userRole);
            return null;
        });
        return user;
    }

    public Optional<User> activateRegistration(String key) {
        return userRepository.findOneByActivationKey(key).map(user -> {
            user.setActivated(true);
            user.setActivationKey(null);
            return user;
        });
    }
}