package com.pocopi.api.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "username", nullable = false, length = 32, unique = true)
    private String username;

    @Size(min = 60, max = 60)
    @NotNull
    @Column(name = "password", nullable = false, length = 60)
    private String password;
}
