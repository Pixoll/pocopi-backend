package com.pocopi.api.models.config;

import com.pocopi.api.models.image.ImageModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int version;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "icon_id")
    private ImageModel icon = null;

    @Size(min = 1, max = 100)
    @NotNull
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Size(min = 1, max = 200)
    @Column(name = "subtitle", length = 200)
    private String subtitle = null;

    @Size(min = 1, max = 2000)
    @NotNull
    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Size(min = 1, max = 2000)
    @NotNull
    @Column(name = "informed_consent", nullable = false, length = 2000)
    private String informedConsent;

    @NotNull
    @Column(name = "anonymous", nullable = false)
    private boolean anonymous = true;
}
