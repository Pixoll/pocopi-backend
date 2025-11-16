package com.pocopi.api.models.config;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.regex.Pattern;

@Entity
@Table(name = "pattern")
@Getter
@Setter
@NoArgsConstructor
public class PatternModel {
    public static final int NAME_MIN_LENGTH = 1;
    public static final int NAME_MAX_LENGTH = 50;
    public static final int REGEX_MIN_LENGTH = 1;
    public static final int REGEX_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @Size(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH)
    @NotNull
    @Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Size(min = REGEX_MIN_LENGTH, max = REGEX_MAX_LENGTH)
    @NotNull
    @Column(name = "regex", nullable = false, length = REGEX_MAX_LENGTH)
    private String regex;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    transient private Pattern _pattern;

    @Builder
    public PatternModel(String name, String regex) {
        this.name = name;
        this.regex = regex;
    }

    public Pattern getPattern() {
        if (_pattern != null) {
            return _pattern;
        }

        _pattern = Pattern.compile(regex);
        return _pattern;
    }
}
