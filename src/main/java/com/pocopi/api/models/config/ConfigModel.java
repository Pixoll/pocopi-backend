package com.pocopi.api.models.config;

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
    public static final int TITLE_MIN_LEN = 1;
    public static final int TITLE_MAX_LEN = 100;
    public static final int SUBTITLE_MIN_LEN = 1;
    public static final int SUBTITLE_MAX_LEN = 200;
    public static final int DESCRIPTION_MIN_LEN = 1;
    public static final int DESCRIPTION_MAX_LEN = 2000;
    public static final int INFORMED_CONSENT_MIN_LEN = 1;
    public static final int INFORMED_CONSENT_MAX_LEN = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int version;

    @Builder.Default
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "icon_id")
    private ImageModel icon = null;

    @Size(min = TITLE_MIN_LEN, max = TITLE_MAX_LEN)
    @NotNull
    @Column(name = "title", nullable = false, length = TITLE_MAX_LEN)
    private String title;

    @Builder.Default
    @Size(min = SUBTITLE_MIN_LEN, max = SUBTITLE_MAX_LEN)
    @Column(name = "subtitle", length = SUBTITLE_MAX_LEN)
    private String subtitle = null;

    @Size(min = DESCRIPTION_MIN_LEN, max = DESCRIPTION_MAX_LEN)
    @NotNull
    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LEN)
    private String description;

    @Size(min = INFORMED_CONSENT_MIN_LEN, max = INFORMED_CONSENT_MAX_LEN)
    @NotNull
    @Column(name = "informed_consent", nullable = false, length = INFORMED_CONSENT_MAX_LEN)
    private String informedConsent;

    @Builder.Default
    @NotNull
    @Column(name = "anonymous", nullable = false)
    private boolean anonymous = true;

    @Builder.Default
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "username_pattern_id")
    private PatternModel usernamePattern = null;
}
