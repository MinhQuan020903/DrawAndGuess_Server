package com.backend.rest.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findByUsername(String username) {
        Optional<User> userFromDb = userRepository.findByUsername(username);
        return userFromDb.orElse(null);
    }

    public User save(User theUser) {
        return userRepository.save(theUser);
    }

}
