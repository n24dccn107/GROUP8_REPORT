package org.example.library_management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "app.business-rules.scheduler.enabled", havingValue = "true", matchIfMissing = true)
@Component
public class BusinessRulesScheduler {
    private static final Logger log = LoggerFactory.getLogger(BusinessRulesScheduler.class);
    private final AccountStatusService accountStatusService;
    private final LibrarianService librarianService;

    public BusinessRulesScheduler(AccountStatusService accountStatusService, LibrarianService librarianService) {
        this.accountStatusService = accountStatusService;
        this.librarianService = librarianService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void refreshHourly() {
        refresh();
    }

    private void refresh() {
        try {
            accountStatusService.disableExpiredMemberAccounts();
            librarianService.refreshAllOverdueFines();
        } catch (RuntimeException exception) {
            log.error("Unable to refresh membership and overdue fine states", exception);
        }
    }
}
