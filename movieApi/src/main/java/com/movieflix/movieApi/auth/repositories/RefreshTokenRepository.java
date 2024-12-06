package com.movieflix.movieApi.auth.repositories;

import com.movieflix.movieApi.auth.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
}
