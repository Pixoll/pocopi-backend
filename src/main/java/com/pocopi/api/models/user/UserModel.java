package com.pocopi.api.models.user;

import com.pocopi.api.converters.RoleConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
public class UserModel {
    public static final int USERNAME_MIN_LEN = 1;
    public static final int USERNAME_MAX_LEN = 32;
    public static final int NAME_MIN_LEN = 1;
    public static final int NAME_MAX_LEN = 50;
    public static final int EMAIL_MIN_LEN = 1;
    public static final int EMAIL_MAX_LEN = 50;
    public static final int AGE_MIN = 1;
    public static final int AGE_MAX = 120;
    public static final int PASSWORD_MIN_LEN = 8;
    public static final int PASSWORD_MAX_LEN = 72;
    public static final int ENCRYPTED_PASSWORD_LEN = 60;
    public static final String AGE_MIN_STR = AGE_MIN + "";
    public static final String AGE_MAX_STR = AGE_MAX + "";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @Length(min = USERNAME_MIN_LEN, max = USERNAME_MAX_LEN)
    @NotNull
    @Column(name = "username", nullable = false, length = USERNAME_MAX_LEN, unique = true)
    private String username;

    @NotNull
    @Convert(converter = RoleConverter.class)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "anonymous", nullable = false)
    private boolean anonymous;

    @Length(min = NAME_MIN_LEN, max = NAME_MAX_LEN)
    @Column(name = "name", length = NAME_MAX_LEN)
    private String name = null;

    @Length(min = EMAIL_MIN_LEN, max = EMAIL_MAX_LEN)
    @Column(name = "email", length = EMAIL_MAX_LEN, unique = true)
    private String email = null;

    @Range(min = AGE_MIN, max = AGE_MAX)
    @Column(name = "age", columnDefinition = "int1 unsigned")
    private Byte age;

    @Length(min = ENCRYPTED_PASSWORD_LEN, max = ENCRYPTED_PASSWORD_LEN)
    @NotNull
    @Column(name = "password", nullable = false, length = ENCRYPTED_PASSWORD_LEN)
    private String password;

    @Builder
    public UserModel(String username, boolean anonymous, String name, String email, Byte age, String password) {
        this.username = username;
        this.anonymous = anonymous;
        this.name = name;
        this.email = email;
        this.age = age;
        this.password = password;
    }
}
