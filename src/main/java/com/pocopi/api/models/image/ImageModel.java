package com.pocopi.api.models.image;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "image")
@Getter
@Setter
@NoArgsConstructor()
@AllArgsConstructor()
@Builder
public class ImageModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @Size(min = 1, max = 512)
    @NotNull
    @Column(name = "path", nullable = false, length = 512, unique = true)
    private String path;

    @Size(min = 1, max = 100)
    @Column(name = "alt", length = 100)
    private String alt = null;
}
