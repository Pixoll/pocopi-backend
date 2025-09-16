package com.pocopi.api.modules.Image;

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
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    @Setter(AccessLevel.NONE)
    private int id;

    @Size(max = 512)
    @NotNull
    @Column(name = "path", nullable = false, length = 512)
    private String path;

    @Size(max = 100)
    @Column(name = "alt", length = 100)
    private String alt;


}
