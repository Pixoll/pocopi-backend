package com.pocopi.api.models.config;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "translation_key")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranslationKeyModel {
    public static final int KEY_MIN_LENGTH = 1;
    public static final int KEY_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @Size(max = KEY_MAX_LENGTH)
    @NotNull
    @Column(name = "`key`", nullable = false, length = KEY_MAX_LENGTH)
    private String key;

    @Size(max = 500)
    @NotNull
    @Column(name = "description", nullable = false, length = 500)
    private String value;

    @NotNull
    @Column(name = "arguments", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> arguments;

}
