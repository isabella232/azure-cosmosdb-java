// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cosmosdb.rx.internal.http.policy;

import com.microsoft.azure.cosmosdb.rx.internal.http.HttpPipelineCallContext;
import com.microsoft.azure.cosmosdb.rx.internal.http.HttpPipelineNextPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.http.HttpRequest;
import com.microsoft.azure.cosmosdb.rx.internal.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * A pipeline policy that retries when a recoverable HTTP error occurs.
 */
public class RetryPolicy implements HttpPipelinePolicy {
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_DELAY = 0;
    private static final ChronoUnit DEFAULT_TIME_UNIT = ChronoUnit.MILLIS;
    private static final String RETRY_AFTER_MS_HEADER = "retry-after-ms";
    private final int maxRetries;
    private final Duration delayDuration;

    /**
     * Creates a RetryPolicy with the default number of retry attempts and delay between retries.
     */
    public RetryPolicy() {
        this.maxRetries = DEFAULT_MAX_RETRIES;
        this.delayDuration = Duration.of(DEFAULT_DELAY, DEFAULT_TIME_UNIT);
    }

    /**
     * Creates a RetryPolicy.
     *
     * @param maxRetries the maximum number of retries to attempt.
     * @param delayDuration the delay between retries
     */
    public RetryPolicy(int maxRetries, Duration delayDuration) {
        this.maxRetries = maxRetries;
        this.delayDuration = delayDuration;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptAsync(context, next, context.httpRequest(), 0);
    }

    private Mono<HttpResponse> attemptAsync(final HttpPipelineCallContext context, final HttpPipelineNextPolicy next, final HttpRequest originalHttpRequest, final int tryCount) {
        context.withHttpRequest(originalHttpRequest.buffer());
        return next.clone().process()
                .flatMap(httpResponse -> {
                    if (shouldRetry(httpResponse, tryCount)) {
                        return attemptAsync(context, next, originalHttpRequest, tryCount + 1).delaySubscription(determineDelayDuration(httpResponse));
                    } else {
                        return Mono.just(httpResponse);
                    }
                })
                .onErrorResume(err -> {
                    if (tryCount < maxRetries) {
                        return attemptAsync(context, next, originalHttpRequest, tryCount + 1).delaySubscription(this.delayDuration);
                    } else {
                        return Mono.error(err);
                    }
                });
    }

    private boolean shouldRetry(HttpResponse response, int tryCount) {
        int code = response.statusCode();
        return tryCount < maxRetries
                && (code == HttpResponseStatus.REQUEST_TIMEOUT.code()
                || (code >= HttpResponseStatus.INTERNAL_SERVER_ERROR.code()
                && code != HttpResponseStatus.NOT_IMPLEMENTED.code()
                && code != HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED.code()));
    }

    /**
     * Determines the delay duration that should be waited before retrying.
     * @param response HTTP response
     * @return If the HTTP response has a retry-after-ms header that will be returned,
     * otherwise the duration used during the construction of the policy.
     */
    private Duration determineDelayDuration(HttpResponse response) {
        int code = response.statusCode();

        // Response will not have a retry-after-ms header.
        if (code != HttpResponseStatus.TOO_MANY_REQUESTS.code()
            && code != HttpResponseStatus.SERVICE_UNAVAILABLE.code()) {
            return this.delayDuration;
        }

        String retryHeader = response.headerValue(RETRY_AFTER_MS_HEADER);

        // Retry header is missing or empty, return the default delay duration.
        if (retryHeader == null || retryHeader.isEmpty()) {
            return this.delayDuration;
        }

        // Use the response delay duration, the server returned it for a reason.
        return Duration.ofMillis(Integer.parseInt(retryHeader));
    }
}