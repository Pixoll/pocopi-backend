package com.pocopi.api.modules.Config;

import com.pocopi.api.modules.HomeInfoCard.HomeInfoCardModel;
import com.pocopi.api.modules.Image.ImageModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "config")
@Getter
@Setter
@NoArgsConstructor()
@AllArgsConstructor()
@Builder
public class ConfigModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version", columnDefinition = "int UNSIGNED not null")
    @Setter(AccessLevel.NONE)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_id")
    private ImageModel icon;

    @Size(max = 100)
    @NotNull
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Size(max = 200)
    @Column(name = "subtitle", length = 200)
    private String subtitle;

    @Size(max = 2000)
    @NotNull
    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Size(max = 2000)
    @NotNull
    @Column(name = "informed_consent", nullable = false, length = 2000)
    private String informedConsent;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "anonymous", nullable = false)
    private boolean anonymous = false;

    @OneToMany(mappedBy = "configVersion")
    private Set<HomeInfoCardModel> homeInfoCards = new LinkedHashSet<>();

}
