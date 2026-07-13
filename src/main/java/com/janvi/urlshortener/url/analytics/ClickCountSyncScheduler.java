package com.janvi.urlshortener.url.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickCountSyncScheduler {

    private final UrlClickCounterService clickCounterService;
    private final ClickCountPersistenceService persistenceService;

    @Scheduled(
            fixedDelayString =
                    "${app.analytics.click-sync-interval-ms:30000}"
    )
    public void syncPendingClicks() {

        Set<String> shortCodes =
                clickCounterService.findShortCodesWithPendingClicks();

        for (String shortCode : shortCodes) {

            long delta =
                    clickCounterService.drainPendingClicks(shortCode);

            if (delta == 0) {
                continue;
            }

            try {
                persistenceService.persistClicks(shortCode, delta);

                log.info(
                        "Persisted {} clicks for shortCode={}",
                        delta,
                        shortCode
                );

            } catch (Exception ex) {

                /*
                 * MySQL update failed after the Redis counter
                 * was drained. Add it back so it can be retried.
                 */
                clickCounterService.restorePendingClicks(
                        shortCode,
                        delta
                );

                log.error(
                        "Failed to persist clicks for shortCode={}. " +
                                "Restored delta={} to Redis.",
                        shortCode,
                        delta,
                        ex
                );
            }
        }
    }
}