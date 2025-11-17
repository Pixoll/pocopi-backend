package com.pocopi.api.repositories;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigModel, String> {
    @NativeQuery(
        """
            select *
                from config
                where version = coalesce(
                    (
                        select version
                            from config
                            where active = true
                            order by version desc
                            limit 1
                        ),
                    (
                        select version
                            from config
                            order by version desc
                            limit 1
                        )
                    )
                limit 1;
            """
    )
    ConfigModel getLastConfig();

    Optional<ConfigModel> findByVersion(int version);

    @NativeQuery(
        """
            select u.*
                from config                      c
                    inner join test_group        g on g.config_version = c.version
                    inner join user_test_attempt ta on ta.group_id = g.id
                    inner join user              u on u.id = ta.user_id
                where c.version = :configVersion
                group by u.id
            """
    )
    List<UserModel> findAllUsersAssociatedWithConfig(int configVersion);

    default boolean hasUsersAssociatedWithConfig(int configVersion) {
        return !findAllUsersAssociatedWithConfig(configVersion).isEmpty();
    }

    void deleteByVersion(int version);
}
