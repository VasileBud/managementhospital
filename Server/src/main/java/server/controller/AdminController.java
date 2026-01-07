package server.controller;

import shared.common.Response;
import shared.dto.AdminStatsDTO;
import shared.dto.AdminUserDTO;
import shared.dto.CommandDTO;
import server.repository.AdminRepository;
import server.repository.DoctorRepository;
import server.repository.PatientRepository;
import server.repository.UserRepository;
import util.PasswordHasher;

import java.time.LocalDate;
import java.util.List;

public class AdminController {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;
    private final PasswordHasher passwordHasher;

    public AdminController() {
        this.userRepository = new UserRepository();
        this.doctorRepository = new DoctorRepository();
        this.patientRepository = new PatientRepository();
        this.adminRepository = new AdminRepository();
        this.passwordHasher = new PasswordHasher();
    }

    public Response listUsers() {
        try {
            List<AdminUserDTO> users = adminRepository.findAdminUsers();
            return Response.ok(users);
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response getAdminStats() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            long totalDoctors = adminRepository.countDoctors();
            long totalPatients = adminRepository.countPatients();
            long activeAccounts = adminRepository.countUsers();
            long newAccountsToday = adminRepository.countUsersOnDate(today);

            long doctorsYesterday = adminRepository.countDoctorsBefore(today);
            long patientsYesterday = adminRepository.countPatientsBefore(today);
            long activeYesterday = adminRepository.countUsersBefore(today);
            long newAccountsYesterday = adminRepository.countUsersOnDate(yesterday);

            AdminStatsDTO stats = new AdminStatsDTO(
                    totalDoctors,
                    totalPatients,
                    activeAccounts,
                    newAccountsToday,
                    percentChange(totalDoctors, doctorsYesterday),
                    percentChange(totalPatients, patientsYesterday),
                    percentChange(activeAccounts, activeYesterday),
                    percentChange(newAccountsToday, newAccountsYesterday)
            );
            return Response.ok(stats);
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response createUser(CommandDTO command) {
        String firstName = command.getString("firstName");
        String lastName = command.getString("lastName");
        String email = command.getString("email");
        String password = command.getString("password");
        String role = command.getString("role");
        String nationalId = command.getString("nationalId");
        Long specializationId = command.getLong("specializationId");

        if (firstName == null || lastName == null || email == null || password == null || role == null) {
            return Response.error("VALIDATION_ERROR", "Missing required fields");
        }
        if ("DOCTOR".equalsIgnoreCase(role) && specializationId == null) {
            return Response.error("VALIDATION_ERROR", "Specialization required for doctor.");
        }

        try {
            if (userRepository.findByEmail(email) != null) {
                return Response.error("EMAIL_EXISTS", "User already exists");
            }

            long userId = userRepository.createUser(
                    firstName, lastName, email,
                    passwordHasher.hash(password), role
            );

            if ("DOCTOR".equalsIgnoreCase(role)) {
                doctorRepository.createDoctor(userId, specializationId);
            }

            if ("PATIENT".equalsIgnoreCase(role)) {
                patientRepository.createPatient(
                        userId,
                        nationalId,
                        null,
                        null,
                        null
                );
            }

            return Response.okMessage("User created successfully");

        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    public Response deleteUser(CommandDTO command) {
        Long userId = command.getLong("userId");
        if (userId == null) {
            return Response.error("VALIDATION_ERROR", "User id is required");
        }

        try {
            Long doctorId = doctorRepository.findDoctorIdByUserId(userId);
            if (doctorId != null && adminRepository.hasAppointmentsForDoctor(doctorId)) {
                return Response.error("HAS_APPOINTMENTS", "Doctor has existing appointments.");
            }

            Long patientId = patientRepository.findPatientIdByUserId(userId);
            if (patientId != null && adminRepository.hasAppointmentsForPatient(patientId)) {
                return Response.error("HAS_APPOINTMENTS", "Patient has existing appointments.");
            }

            boolean deleted = userRepository.deleteUser(userId);
            if (deleted) return Response.okMessage("User deleted");
            else return Response.error("NOT_FOUND", "User not found");
        } catch (Exception e) {
            return Response.error("DB_ERROR", "User could not be deleted: " + e.getMessage());
        }
    }

    public Response updateUser(CommandDTO command) {
        Long userId = command.getLong("userId");
        String firstName = command.getString("firstName");
        String lastName = command.getString("lastName");
        String email = command.getString("email");
        String password = command.getString("password");
        String role = command.getString("role");
        String nationalId = command.getString("nationalId");
        Long specializationId = command.getLong("specializationId");

        if (userId == null || firstName == null || lastName == null || email == null || role == null) {
            return Response.error("VALIDATION_ERROR", "Missing required fields");
        }

        try {
            Long doctorId = null;
            if ("DOCTOR".equalsIgnoreCase(role)) {
                doctorId = doctorRepository.findDoctorIdByUserId(userId);
                if (doctorId == null && specializationId == null) {
                    return Response.error("VALIDATION_ERROR", "Specialization required for doctor.");
                }
            }

            var existingByEmail = userRepository.findByEmail(email);
            if (existingByEmail != null && existingByEmail.getUserId() != userId) {
                return Response.error("EMAIL_EXISTS", "Email already exists");
            }

            boolean updated = userRepository.updateUser(userId, firstName, lastName, email, role);
            if (!updated) {
                return Response.error("NOT_FOUND", "User not found");
            }

            if (password != null && !password.isBlank()) {
                userRepository.updatePassword(userId, passwordHasher.hash(password));
            }

            if ("DOCTOR".equalsIgnoreCase(role)) {
                if (specializationId != null) {
                    if (doctorId == null) {
                        doctorRepository.createDoctor(userId, specializationId);
                    } else {
                        doctorRepository.updateDoctorSpecialization(doctorId, specializationId);
                    }
                }
            }

            if ("PATIENT".equalsIgnoreCase(role)) {
                Long patientId = patientRepository.findPatientIdByUserId(userId);
                if (patientId == null) {
                    patientRepository.createPatient(userId, nationalId, null, null, null);
                } else if (nationalId != null) {
                    patientRepository.updateNationalIdByUserId(userId, nationalId);
                }
            }

            return Response.okMessage("User updated successfully");
        } catch (Exception e) {
            return Response.error("DB_ERROR", e.getMessage());
        }
    }

    private double percentChange(double current, double previous) {
        if (previous == 0) {
            return current == 0 ? 0.0 : 100.0;
        }
        return ((current - previous) / previous) * 100.0;
    }
}
