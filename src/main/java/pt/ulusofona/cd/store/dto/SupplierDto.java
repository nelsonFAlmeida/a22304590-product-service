package pt.ulusofona.cd.store.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDto {
    private UUID id;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 200, message = "Address must be between 10 and 200 characters")
    private String address;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    private String phoneNumber;

    @Size(max = 50, message = "Contact person must not exceed 50 characters")
    private String contactPerson;

    @Size(max = 20, message = "Tax number must not exceed 20 characters")
    private String taxNumber;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in YYYY-MM-DD format")
    private String establishedDate;

    private boolean isActive = true;
    private Instant createdAt;
    private Instant updatedAt;
}