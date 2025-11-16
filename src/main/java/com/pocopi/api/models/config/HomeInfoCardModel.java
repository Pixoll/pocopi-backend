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
    public static final int TITLE_MIN_LEN = 1;
    public static final int TITLE_MAX_LEN = 50;
    public static final int DESCRIPTION_MIN_LEN = 1;
    public static final int DESCRIPTION_MAX_LEN = 100;

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

    @Size(min = TITLE_MIN_LEN, max = TITLE_MAX_LEN)
    @NotNull
    @Column(name = "title", nullable = false, length = TITLE_MAX_LEN)
    private String title;

    @Size(min = DESCRIPTION_MIN_LEN, max = DESCRIPTION_MAX_LEN)
    @NotNull
    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LEN)
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
