package com.movieflix.movieApi.auth.repositories;

import com.movieflix.movieApi.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {


}
