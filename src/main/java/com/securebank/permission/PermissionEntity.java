package com.securebank.permission;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;        // e.g. "loan:approve"

    private String description;

    @Column(precision = 19, scale = 4)
    private BigDecimal maxAmount; // null = no limit


}
