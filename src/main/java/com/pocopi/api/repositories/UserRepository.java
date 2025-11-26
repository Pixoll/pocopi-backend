package com.pocopi.api.repositories;

import com.pocopi.api.models.user.Role;
import com.pocopi.api.models.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Integer> {
    @NativeQuery("select * from user u where u.id = :userId")
    UserModel getUserByUserId(@Param("userId") int userId);

    boolean existsByUsername(String username);

    Optional<UserModel> findByUsername(String username);

    boolean existsByEmail(String email);

    @NativeQuery(
        """
            select u.*
                from user_test_attempt ta
                    inner join user    u on u.id = ta.user_id
                where ta.id in :attemptIds
                group by u.id
            """
    )
    List<UserModel> findAllUsersByAttemptIds(List<Long> attemptIds);

    List<UserModel> findAllByRole(Role role);
}
