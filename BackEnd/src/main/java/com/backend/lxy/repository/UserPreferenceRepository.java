package com.backend.lxy.repository;

import com.backend.lxy.domain.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    UserPreference findFirstByOrderByIdAsc();
}
