package com.tdtsqlscan.user.service;

import com.tdtsqlscan.user.domain.User;
import com.tdtsqlscan.user.domain.UserRepository;
import com.tdtsqlscan.user.dto.CreateUserRequestDto;
import com.tdtsqlscan.user.dto.UpdateUserRequestDto;
import com.tdtsqlscan.user.dto.UserResponseDto;
import com.tdtsqlscan.user.exception.UserAlreadyExistsException;
import com.tdtsqlscan.user.exception.UserNotFoundException;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(
            UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto requestDto) {
        if (userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "User with username " + requestDto.getUsername() + " already exists.");
        }
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "User with email " + requestDto.getEmail() + " already exists.");
        }

        User user = new User();
        user.setUsername(requestDto.getUsername());
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setEnabled(true); // Default to enabled

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return userMapper.toUserResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUser(UUID id, UpdateUserRequestDto requestDto) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (requestDto.getUsername() != null) {
            userRepository
                    .findByUsername(requestDto.getUsername())
                    .ifPresent(
                            existingUser -> {
                                if (!existingUser.getId().equals(id)) {
                                    throw new UserAlreadyExistsException(
                                            "Username " + requestDto.getUsername() + " is already taken.");
                                }
                            });
            user.setUsername(requestDto.getUsername());
        }

        if (requestDto.getEmail() != null) {
            userRepository
                    .findByEmail(requestDto.getEmail())
                    .ifPresent(
                            existingUser -> {
                                if (!existingUser.getId().equals(id)) {
                                    throw new UserAlreadyExistsException(
                                            "Email " + requestDto.getEmail() + " is already taken.");
                                }
                            });
            user.setEmail(requestDto.getEmail());
        }

        if (requestDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        }

        if (requestDto.getEnabled() != null) {
            user.setEnabled(requestDto.getEnabled());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
