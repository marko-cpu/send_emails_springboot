package io.email.userservice.service;

import io.email.userservice.domain.User;

public interface UserService {

    User saveUser(User user);
    Boolean verifyToken(String token);


}
