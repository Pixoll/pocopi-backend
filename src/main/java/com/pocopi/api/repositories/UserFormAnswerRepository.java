package com.pocopi.api.repositories;

import com.pocopi.api.models.form.UserFormAnswerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFormAnswerRepository extends JpaRepository<UserFormAnswerModel, Integer> {
    List<UserFormAnswerModel> findAllByUser_Id(int userId);
}