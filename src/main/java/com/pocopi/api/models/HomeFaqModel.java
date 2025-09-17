package com.pocopi.api.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "home_faq", uniqueConstraints = @UniqueConstraint(columnNames = {"config_version", "order"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeFaqModel {
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

    @Column(name = "order", nullable = false, columnDefinition = "int1 unsigned")
    private byte order;

    @Size(min = 1, max = 100)
    @NotNull
    @Column(name = "question", nullable = false, length = 100)
    private String question;

    @Size(min = 1, max = 500)
    @NotNull
    @Column(name = "answer", nullable = false, length = 500)
    private String answer;
}
