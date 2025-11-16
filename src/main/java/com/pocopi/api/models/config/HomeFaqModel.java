package com.pocopi.api.models.config;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "home_faq", uniqueConstraints = @UniqueConstraint(columnNames = {"config_version", "`order`"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeFaqModel {
    public static final int QUESTION_MIN_LEN = 1;
    public static final int QUESTION_MAX_LEN = 100;
    public static final int ANSWER_MIN_LEN = 1;
    public static final int ANSWER_MAX_LEN = 500;

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

    @Size(min = QUESTION_MIN_LEN, max = QUESTION_MAX_LEN)
    @NotNull
    @Column(name = "question", nullable = false, length = QUESTION_MAX_LEN)
    private String question;

    @Size(min = ANSWER_MIN_LEN, max = ANSWER_MAX_LEN)
    @NotNull
    @Column(name = "answer", nullable = false, length = ANSWER_MAX_LEN)
    private String answer;
}
