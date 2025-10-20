package pt.ulusofona.cd.store.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record Stock(
        @NotNull @Min(1) Integer quantity
) {}
