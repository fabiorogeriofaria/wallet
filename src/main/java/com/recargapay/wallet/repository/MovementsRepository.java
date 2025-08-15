package com.recargapay.wallet.repository;

import com.recargapay.wallet.domain.Movements;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementsRepository extends JpaRepository<Movements, Long> {
    List<Movements> findByWalletIdAndOccurredAtLessThanEqualOrderByOccurredAtAsc(String walletId, OffsetDateTime occurredAt);
    List<Movements> findByWalletIdOrderByOccurredAtAsc(String walletId);
}


