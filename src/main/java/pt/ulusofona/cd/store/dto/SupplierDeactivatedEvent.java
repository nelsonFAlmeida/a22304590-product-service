package pt.ulusofona.cd.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDeactivatedEvent {

    private String supplierId;
    private String companyName;
    private String email;
    private boolean isActive;
    private LocalDateTime deactivationDate;

}