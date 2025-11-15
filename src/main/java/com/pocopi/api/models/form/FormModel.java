package com.pocopi.api.models.form;

import com.pocopi.api.converters.FormTypeConverter;
import com.pocopi.api.models.config.ConfigModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "form", uniqueConstraints = {@UniqueConstraint(columnNames = {"config_version", "type"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "config_version", nullable = false)
    private ConfigModel config;

    @Builder.Default
    @Size(min = 1, max = 100)
    @Column(name = "title", length = 100)
    private String title = null;

    @NotNull
    @Convert(converter = FormTypeConverter.class)
    @Column(name = "type", nullable = false)
    private FormType type;
}
