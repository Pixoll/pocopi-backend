package com.pocopi.api.modules.forms;

import com.pocopi.api.modules.Config.ConfigModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "form")
@Getter
@Setter
public class FormModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "config_version", nullable = false)
    private ConfigModel configVersion;

    @NotNull
    @Lob
    @Column(name = "type", nullable = false)
    private FormType type;

    @Size(max = 100)
    @Column(name = "title", length = 100)
    private String title;

}
