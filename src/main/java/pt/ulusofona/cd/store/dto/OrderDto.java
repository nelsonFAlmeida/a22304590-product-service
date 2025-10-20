package pt.ulusofona.cd.store.dto;

import java.util.UUID;

public class OrderDto {
    private UUID id;
    private String email;
    private UUID product_id;
    private int quantity;
    private Boolean is_confirmed;
}