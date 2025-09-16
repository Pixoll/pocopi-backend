package com.pocopi.api.modules.HomeInfoCard;

import com.pocopi.api.modules.Config.ConfigModel;
import com.pocopi.api.modules.Image.ImageModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "home_info_card",
        uniqueConstraints=
        @UniqueConstraint(columnNames={"config_version", "icon_id"}))
@Getter
@Setter
@NoArgsConstructor()
@AllArgsConstructor()
@Builder
public class HomeInfoCardModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    @Getter
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "config_version", nullable = false)
    private ConfigModel configVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_id")
    private ImageModel icon;

    @Column(name = "order", columnDefinition = "tinyint UNSIGNED not null")
    private byte order;

    @Size(max = 50)
    @NotNull
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Size(max = 100)
    @NotNull
    @Column(name = "description", nullable = false, length = 100)
    private String description;

    @Column(name = "color")
    private int color;
}


