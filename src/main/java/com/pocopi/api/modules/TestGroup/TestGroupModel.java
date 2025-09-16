package com.pocopi.api.modules.TestGroup;

import com.pocopi.api.modules.Config.ConfigModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "test_group", uniqueConstraints = {@UniqueConstraint(columnNames = {"label", "config_version"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestGroupModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "config_version", nullable = false)
    private ConfigModel configVersion;

    @Size(max = 25)
    @NotNull
    @Column(name = "label", nullable = false, length = 25)
    private String label;

    @Column(name = "probability", columnDefinition = "tinyint UNSIGNED not null")
    private byte probability;

    @Size(max = 2000)
    @Column(name = "greeting", length = 2000)
    private String greeting;

}
