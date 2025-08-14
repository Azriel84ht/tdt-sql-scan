package com.tdtsqlscan.web.service;

import com.tdtsqlscan.web.domain.User;
import com.tdtsqlscan.web.domain.VerificationToken;
import com.tdtsqlscan.web.dto.RegistrationResult;
import com.tdtsqlscan.web.dto.UserDto;
import com.tdtsqlscan.web.domain.PasswordResetToken;
import java.util.Optional;
import com.tdtsqlscan.web.repository.PasswordResetTokenRepository;
import com.tdtsqlscan.web.repository.UserRepository;
import com.tdtsqlscan.web.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegistrationResult registerNewUserAccount(UserDto userDto) throws Exception {
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new Exception("There is an account with that username: " + userDto.getUsername());
        }

        // Check if email exists
        final Optional<User> userOptional = userRepository.findByEmail(userDto.getEmail());
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            if (existingUser.isEnabled()) {
                // User exists and is verified, throw error
                throw new Exception("There is an account with that email address: " + userDto.getEmail());
            } else {
                // User exists but is not verified, signal to resend email
                return new RegistrationResult(existingUser, true);
            }
        }

        // Create a new user if no account with that email exists
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRoles("USER");
        user.setEnabled(false);
        User savedUser = userRepository.save(user);
        return new RegistrationResult(savedUser, false);
    }

    public void createVerificationTokenForUser(final User user, final String token) {
        // A user should only have one active verification token. Delete any existing ones.
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

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

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken myToken = new PasswordResetToken(user);
        myToken.setToken(token);
        passwordResetTokenRepository.save(myToken);
    }

    public String validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = passwordResetTokenRepository.findByToken(token).orElse(null);

        if (passToken == null) {
            return "invalidToken";
        }

        if (passToken.getExpiryDate().before(new Date())) {
            return "expired";
        }

        return null; // Token is valid
    }

    public User getUserByPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .map(PasswordResetToken::getUser)
                .orElse(null);
    }

    public void changeUserPassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        user.setMustChangePassword(false); // Assuming a direct change clears the flag.
        userRepository.save(user);
    }

    public void forcePasswordChange(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        user.setMustChangePassword(true);
        userRepository.save(user);
    }
}
