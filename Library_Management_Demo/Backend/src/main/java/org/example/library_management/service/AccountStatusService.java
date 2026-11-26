package org.example.library_management.service;

import org.example.library_management.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountStatusService {
    private final AccountRepository accountRepository;

    public AccountStatusService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void disableAccount(long accountId) {
        accountRepository.disableAccount(accountId);
    }

    @Transactional
    public int disableExpiredMemberAccounts() {
        return accountRepository.disableExpiredMemberAccounts();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void disableMemberAccount(long personId) {
        accountRepository.disableMemberAccount(personId);
    }
}
