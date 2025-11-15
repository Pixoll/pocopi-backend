package com.pocopi.api.models.config;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "home_info_card", uniqueConstraints = @UniqueConstraint(columnNames = {"config_version", "`order`"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeInfoCardModel {
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

    @Column(name = "`order`", nullable = false, columnDefinition = "int1 unsigned")
    private byte order;

    @Size(min = 1, max = 50)
    @NotNull
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Size(min = 1, max = 100)
    @NotNull
    @Column(name = "description", nullable = false, length = 100)
    private String description;

    @Builder.Default
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "icon_id")
    private ImageModel icon = null;

    @Builder.Default
    @Max(0xffffff)
    @Column(name = "color", columnDefinition = "int3 unsigned")
    private Integer color = null;
}
