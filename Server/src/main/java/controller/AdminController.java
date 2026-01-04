package controller;

import common.Response;
import dto.CommandDTO;
import dto.UserDTO;
import repository.UserRepository;
import repository.DoctorRepository;
import util.PasswordHasher;

import java.util.List;

public class AdminController {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordHasher passwordHasher;

    public AdminController() {
        this.userRepository = new UserRepository();
        this.doctorRepository = new DoctorRepository();
        this.passwordHasher = new PasswordHasher();
    }

    public Response listUsers() {
        try {
            // Metoda findAllUsers() trebuie să returneze List<UserDTO>
            List<UserDTO> users = userRepository.findAllUsers();
            return Response.ok(users);
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response createUser(CommandDTO command) {
        String firstName = command.getString("firstName");
        String lastName = command.getString("lastName");
        String email = command.getString("email");
        String password = command.getString("password");
        String role = command.getString("role"); // "DOCTOR", "MANAGER", etc.

        try {
            if (userRepository.findByEmail(email) != null) {
                return Response.error("EMAIL_EXISTS", "User already exists");
            }

            long userId = userRepository.createUser(
                    firstName, lastName, email,
                    passwordHasher.hash(password), role
            );

            // Dacă e DOCTOR, trebuie să creăm și intrarea în tabela 'doctor'
            if ("DOCTOR".equals(role)) {
                Long specializationId = command.getLong("specializationId");
                if (specializationId != null) {
                    doctorRepository.createDoctor(userId, specializationId);
                }
            }

            return Response.okMessage("User created successfully");

        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response deleteUser(CommandDTO command) {
        Long userId = command.getLong("userId");
        try {
            // Trebuie implementat deleteUser în UserRepository (cu CASCADE în DB e mai ușor)
            boolean deleted = userRepository.deleteUser(userId);
            if (deleted) return Response.okMessage("User deleted");
            else return Response.error("NOT_FOUND", "User not found");
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response updateUser(CommandDTO command) {
        // Implementare similară pentru update
        return Response.error("NOT_IMPLEMENTED", "Update user not implemented yet");
    }
}