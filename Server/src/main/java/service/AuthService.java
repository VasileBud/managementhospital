package service;

import jdk.jshell.execution.Util;
import model.User;
import repository.UserRepository;

import java.sql.SQLException;

public class AuthService {
    private UserRepository userRepository = new UserRepository();
    public String register(User user) {
        try {
            if(userRepository.findByEmail(user.getEmail()) != null) {
                return "ERR_EMAIL_EXISTS";
            }

            if(userRepository.save(user)) {
                return "OK_USER_CREATED";
            }
        } catch (SQLException e) {
            return "ERR_DATABASE_ERROR: " + e.getMessage();
        }
        return "ERR_UNKNOWN";
    }
}
