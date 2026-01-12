package controller;

import model.common.Response;
import model.dto.CommandDTO;
import model.dto.LoginResponseDTO;
import model.repository.UserRepository;
import model.repository.PatientRepository;
import model.repository.DoctorRepository;
import util.PasswordHasher;
import model.User;

import java.time.LocalDate;

public class AuthController {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordHasher passwordHasher;

    public AuthController() {
        this.userRepository = new UserRepository();
        this.patientRepository = new PatientRepository();
        this.doctorRepository = new DoctorRepository();
        this.passwordHasher = new PasswordHasher();
    }

    public Response login(CommandDTO command) {

        String email = command.getString("email");
        String password = command.getString("password");

        if (email == null || password == null) {
            return Response.error("VALIDATION_ERROR", "Email and password are required");
        }

        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return Response.error("AUTH_FAILED", "Invalid credentials");
            }

            if (!passwordHasher.verify(password, user.getPasswordHash())) {
                return Response.error("AUTH_FAILED", "Invalid credentials");
            }

            String roleName = userRepository.findRoleNameByUserId(user.getUserId());

            Long patientId = null;
            Long doctorId = null;

            if ("PATIENT".equals(roleName)) {
                patientId = patientRepository.findPatientIdByUserId(user.getUserId());
                if (patientId == null) {
                    patientId = patientRepository.createPatient(
                            user.getUserId(),
                            null,
                            null,
                            null,
                            null
                    );
                }
            } else if ("DOCTOR".equals(roleName)) {
                doctorId = doctorRepository.findDoctorIdByUserId(user.getUserId());
            }

            LoginResponseDTO response = new LoginResponseDTO(
                    user.getUserId(),
                    roleName,
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    patientId,
                    doctorId
            );

            return Response.ok(response);

        } catch (Exception e) {
            return Response.error("SERVER_ERROR", e.getMessage());
        }
    }

    public Response registerPatient(CommandDTO command) {

        String firstName = command.getString("firstName");
        String lastName = command.getString("lastName");
        String email = command.getString("email");
        String password = command.getString("password");

        String nationalId = command.getString("nationalId");
        String address = command.getString("address");
        String phone = command.getString("phone");
        LocalDate birthDate = command.getDate("birthDate");

        if (firstName == null || lastName == null || email == null || password == null) {
            return Response.error("VALIDATION_ERROR", "Missing required fields");
        }

        try {
            if (userRepository.findByEmail(email) != null) {
                return Response.error("EMAIL_EXISTS", "Email already exists");
            }

            String passwordHash = passwordHasher.hash(password);

            long userId = userRepository.createUser(
                    firstName,
                    lastName,
                    email,
                    passwordHash,
                    "PATIENT"
            );

            patientRepository.createPatient(
                    userId,
                    nationalId,
                    address,
                    phone,
                    birthDate
            );

            return Response.okMessage("Registration successful");

        } catch (Exception e) {
            return Response.error("SERVER_ERROR", e.getMessage());
        }
    }

    public Response logout(CommandDTO command) {
        return Response.okMessage("Logged out");
    }
}
