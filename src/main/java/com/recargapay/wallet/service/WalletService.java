package com.recargapay.wallet.service;

import com.recargapay.wallet.domain.Movements;
import com.recargapay.wallet.domain.Wallet;
import com.recargapay.wallet.repository.MovementsRepository;
import com.recargapay.wallet.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final MovementsRepository movementsRepository;

    public WalletService(WalletRepository walletRepository, MovementsRepository movementsRepository) {
        this.walletRepository = walletRepository;
        this.movementsRepository = movementsRepository;
    }

    @Transactional
    public Wallet createWallet(String ownerName) {
        String id = UUID.randomUUID().toString();
        Wallet wallet = new Wallet(id, ownerName);
        Wallet saved = walletRepository.save(wallet);
        logger.info("Wallet created: id={}, ownerName={}", saved.getId(), saved.getOwnerName());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Wallet> listWallets() {
        List<Wallet> wallets = walletRepository.findAll();
        logger.info("Listing all wallets. Count={}", wallets.size());
        return wallets;
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(String walletId) {
        ensureExists(walletId);
        BigDecimal balance = calculateBalance(entriesUntil(walletId, null));
        logger.info("Balance retrieved for walletId={}: {}", walletId, balance);
        return balance;
    }

    @Transactional(readOnly = true)
    public BigDecimal getHistoricalBalance(String walletId, OffsetDateTime at) {
        ensureExists(walletId);
        BigDecimal balance = calculateBalance(entriesUntil(walletId, at));
        logger.info("Historical balance retrieved for walletId={} at {}: {}", walletId, at, balance);
        return balance;
    }

    @Transactional
    public void deposit(String walletId, BigDecimal amount) {
        ensureExists(walletId);
        append(walletId, Movements.Type.DEPOSIT, amount, null);
        logger.info("Deposit: walletId={}, amount={}", walletId, amount);
    }

    @Transactional
    public void withdraw(String walletId, BigDecimal amount) {
        ensureExists(walletId);
        BigDecimal current = getBalance(walletId);
        if (current.compareTo(amount) < 0) {
            logger.warn("Withdraw failed: insufficient funds. walletId={}, requested={}, available={}", walletId, amount, current);
            throw new IllegalStateException("Saldo insuficiente");
        }
        append(walletId, Movements.Type.WITHDRAW, amount, null);
        logger.info("Withdraw: walletId={}, amount={}", walletId, amount);
    }

    @Transactional
    public void transfer(String fromWalletId, String toWalletId, BigDecimal amount) {
        ensureExists(fromWalletId);
        ensureExists(toWalletId);
        BigDecimal current = getBalance(fromWalletId);
        if (current.compareTo(amount) < 0) {
            logger.warn("Transfer failed: insufficient funds. fromWalletId={}, toWalletId={}, requested={}, available={}", fromWalletId, toWalletId, amount, current);
            throw new IllegalStateException("Saldo insuficiente");
        }
        String correlationId = UUID.randomUUID().toString();
        append(fromWalletId, Movements.Type.TRANSFER_OUT, amount, correlationId);
        append(toWalletId, Movements.Type.TRANSFER_IN, amount, correlationId);
        logger.info("Transfer: fromWalletId={}, toWalletId={}, amount={}, correlationId={}", fromWalletId, toWalletId, amount, correlationId);
    }

    private void append(String walletId, Movements.Type type, BigDecimal amount, String correlationId) {
        Movements entry = new Movements();
        entry.setWalletId(walletId);
        entry.setType(type);
        entry.setAmount(amount);
        entry.setOccurredAt(OffsetDateTime.now());
        entry.setCorrelationId(correlationId);
        movementsRepository.save(entry);
    }

    private List<Movements> entriesUntil(String walletId, OffsetDateTime at) {
        return at == null
                ? movementsRepository.findByWalletIdOrderByOccurredAtAsc(walletId)
                : movementsRepository.findByWalletIdAndOccurredAtLessThanEqualOrderByOccurredAtAsc(walletId, at);
    }

    private BigDecimal calculateBalance(List<Movements> entries) {
        BigDecimal balance = BigDecimal.ZERO;
        for (Movements e : entries) {
            switch (e.getType()) {
                case DEPOSIT, TRANSFER_IN -> balance = balance.add(e.getAmount());
                case WITHDRAW, TRANSFER_OUT -> balance = balance.subtract(e.getAmount());
            }
        }
        return balance;
    }

    private void ensureExists(String walletId) {
        walletRepository.findById(walletId).orElseThrow(() -> new EntityNotFoundException("Carteira não encontrada"));
    }
}


