package com.tdtsqlscan.web.service;

import com.tdtsqlscan.web.domain.User;
import com.tdtsqlscan.web.domain.VerificationToken;
import com.tdtsqlscan.web.dto.UserDto;
import com.tdtsqlscan.web.repository.UserRepository;
import com.tdtsqlscan.web.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerNewUserAccount(UserDto userDto) throws Exception {
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new Exception("There is an account with that username: " + userDto.getUsername());
        }
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new Exception("There is an account with that email address: " + userDto.getEmail());
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRoles("USER");
        user.setEnabled(false);
        return userRepository.save(user);
    }

    public void createVerificationTokenForUser(final User user, final String token) {
        final VerificationToken myToken = new VerificationToken(user);
        myToken.setToken(token);
        tokenRepository.save(myToken);
    }

    public VerificationToken getVerificationToken(final String verificationToken) {
        return tokenRepository.findByToken(verificationToken).orElse(null);
    }

    public void saveRegisteredUser(final User user) {
        userRepository.save(user);
    }

    public String validateVerificationToken(String token) {
        final VerificationToken verificationToken = tokenRepository.findByToken(token).orElse(null);
        if (verificationToken == null) {
            return "invalidToken";
        }

        final User user = verificationToken.getUser();
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            tokenRepository.delete(verificationToken);
            return "expired";
        }

        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
        return null;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
