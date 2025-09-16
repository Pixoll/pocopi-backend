package com.pocopi.api.modules.HomeFaq;

import com.pocopi.api.modules.Config.ConfigModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "home_faq", uniqueConstraints=
@UniqueConstraint(columnNames={"config_version", "order"}))
@Getter
@Setter
public class HomeFaqModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "config_version", nullable = false)
    private ConfigModel configVersion;

    @Column(name = "order", columnDefinition = "tinyint UNSIGNED not null")
    private byte order;

    @Size(max = 100)
    @NotNull
    @Column(name = "question", nullable = false, length = 100)
    private String question;

    @Size(max = 500)
    @NotNull
    @Column(name = "answer", nullable = false, length = 500)
    private String answer;

}
