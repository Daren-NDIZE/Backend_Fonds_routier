package com.api.backend.Fonds_routier.repository;

import com.api.backend.Fonds_routier.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode,Long>  {
}
