package com.pocopi.api.models.user;

import com.pocopi.api.models.test.TestGroupModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "user", indexes = {@Index(columnList = "group_id")})
@Getter
@Setter
@NoArgsConstructor
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "username", nullable = false, length = 32, unique = true)
    private String username;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "group_id", nullable = false)
    private TestGroupModel group;

    @Column(name = "anonymous", nullable = false)
    private boolean anonymous;

    @Size(min = 1, max = 50)
    @Column(name = "name", length = 50)
    private String name = null;

    @Size(min = 1, max = 50)
    @Column(name = "email", length = 50, unique = true)
    private String email = null;

    @Column(name = "age", columnDefinition = "int1 unsigned")
    private byte age;

    @Size(min = 60, max = 60)
    @NotNull
    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Builder
    public UserModel(String username, TestGroupModel group, boolean anonymous, String name, String email, Byte age, String password) {
        this.username = username;
        this.group = group;
        this.anonymous = anonymous;
        this.name = name;
        this.email = email;
        this.age = age;
        this.password = password;
    }
}
