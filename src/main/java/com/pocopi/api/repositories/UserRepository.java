package com.pocopi.api.repositories;

import com.pocopi.api.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<UserModel, Integer> {

    @Query(value = "SELECT * from user", nativeQuery = true)
    List<UserModel> getAllUsers();
}
