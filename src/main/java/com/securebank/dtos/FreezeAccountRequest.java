package com.securebank.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FreezeAccountRequest {

    @NotBlank(message = "Reason is required")
    private String reason;

}
