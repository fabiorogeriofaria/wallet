package com.recargapay.wallet.web;

import com.recargapay.wallet.domain.Wallet;
import com.recargapay.wallet.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@Validated
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    public record CreateWalletRequest(@NotBlank String ownerName) {}
    public record CreateWalletResponse(String id, String ownerName) {}
    public record BalanceResponse(BigDecimal balance) {}
    public record TransferRequest(@NotBlank String fromWalletId, @NotBlank String toWalletId, @NotNull BigDecimal amount) {}

    @PostMapping
    public ResponseEntity<CreateWalletResponse> create(@Valid @RequestBody CreateWalletRequest request) {
        Wallet wallet = walletService.createWallet(request.ownerName());
        return ResponseEntity.ok(new CreateWalletResponse(wallet.getId(), wallet.getOwnerName()));
    }

    @GetMapping
    public ResponseEntity<List<Wallet>> list() {
        return ResponseEntity.ok(walletService.listWallets());
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BalanceResponse> balance(@PathVariable String walletId) {
        return ResponseEntity.ok(new BalanceResponse(walletService.getBalance(walletId)));
    }

    @GetMapping("/{walletId}/balance/history")
    public ResponseEntity<BalanceResponse> balanceOnDate(@PathVariable String walletId,
                                                         @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        var endOfDay = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).minusNanos(1);
        return ResponseEntity.ok(new BalanceResponse(walletService.getHistoricalBalance(walletId, endOfDay)));
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<Void> deposit(@PathVariable String walletId, @RequestParam BigDecimal amount) {
        walletService.deposit(walletId, amount);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<Void> withdraw(@PathVariable String walletId, @RequestParam BigDecimal amount) {
        walletService.withdraw(walletId, amount);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        walletService.transfer(request.fromWalletId(), request.toWalletId(), request.amount());
        return ResponseEntity.accepted().build();
    }
}


