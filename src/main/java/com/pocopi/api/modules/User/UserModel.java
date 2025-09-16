package com.pocopi.api.modules.User;

import com.pocopi.api.modules.TestGroup.TestGroupModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    @Setter(AccessLevel.NONE)
    private int id;

    @Size(max = 32)
    @NotNull
    @Column(name = "username", nullable = false, length = 32)
    private String username;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private TestGroupModel group;

    @NotNull
    @Column(name = "anonymous", nullable = false)
    private boolean anonymous;

    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name = null;

    @Size(max = 50)
    @Column(name = "email", length = 50)
    private String email = null;

    @Column(name = "age", columnDefinition = "tinyint UNSIGNED")
    private byte age;

    @Size(max = 60)
    @NotNull
    @Column(name = "password", nullable = false, length = 60)
    private String password;

}
