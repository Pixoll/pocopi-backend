package com.pocopi.api.repositories;

import com.pocopi.api.models.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Integer> {
    @Query(value = "select * from user u where u.id = :userId", nativeQuery = true)
    UserModel getUserByUserId(@Param("userId") int userId);

    @Query(value = "select u.id from user u", nativeQuery = true)
    List<Integer> getAllUserIds();

    boolean existsByUsername(String username);

    Optional<UserModel> findByUsername(String username);

    boolean existsByEmail(String email);

    @Query(value = "select * from user", nativeQuery = true)
    List<UserModel> getAllUsers();
}
