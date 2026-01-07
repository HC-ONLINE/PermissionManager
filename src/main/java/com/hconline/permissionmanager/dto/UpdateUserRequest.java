package com.hconline.permissionmanager.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @Email(message = "Debe ser un email válido")
    private String email;
    
    private Set<Long> roleIds;
}
