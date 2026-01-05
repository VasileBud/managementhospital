package shared.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class RegisterDTO implements Serializable {
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    private String nationalId;
    private String address;
    private String phone;
    private LocalDate birthDate;

    public RegisterDTO(String firstName, String lastName, String email, String password,
                       String nationalId, String address, String phone, LocalDate birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.nationalId = nationalId;
        this.address = address;
        this.phone = phone;
        this.birthDate = birthDate;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNationalId() { return nationalId; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public LocalDate getBirthDate() { return birthDate; }
}
