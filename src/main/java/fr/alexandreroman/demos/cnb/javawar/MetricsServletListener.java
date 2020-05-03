/*
 * Copyright (c) 2020 VMware, Inc. or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alexandreroman.demos.cnb.javawar;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@WebListener
public class MetricsServletListener implements ServletContextListener {
    private static final String SC_METRICS_REGISTRY = "metricsRegistry";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static PrometheusMeterRegistry getPrometheusMeterRegistry(ServletContext context) {
        return (PrometheusMeterRegistry) context.getAttribute(SC_METRICS_REGISTRY);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final var registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        Metrics.addRegistry(registry);

        final var tags = Tags.of("application", "cnb-javawar");
        new JvmMemoryMetrics(tags).bindTo(registry);
        new JvmGcMetrics(tags).bindTo(registry);
        new JvmThreadMetrics(tags).bindTo(registry);
        addCustomMetrics(registry, tags);

        sce.getServletContext().setAttribute(SC_METRICS_REGISTRY, registry);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        final var registry = getPrometheusMeterRegistry(sce.getServletContext());
        if (registry != null) {
            Metrics.removeRegistry(registry);
            sce.getServletContext().removeAttribute(SC_METRICS_REGISTRY);
        }
    }

    private void addCustomMetrics(MeterRegistry reg, Tags commonTags) {
        var tags = Tags.concat(commonTags, Tags.of("java.version", System.getProperty("java.version")));
        final var openSslVersion = getOpenSslVersion();
        if (openSslVersion != null) {
            tags = tags.and("openssl.version", openSslVersion);
        }
        Counter.builder("app.info").description("Get application info")
                .tags(tags).register(reg).increment();
    }

    private String getOpenSslVersion() {
        try {
            final var proc = new ProcessBuilder("openssl", "version", "-a").start();
            if (proc.waitFor() != 0) {
                logger.warn("Unable to get OpenSSL version");
            } else {
                try (final var in = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    final var lines = in.lines().collect(Collectors.toUnmodifiableList());
                    if (!lines.isEmpty()) {
                        final var buf = new StringBuilder(lines.get(0).trim());
                        if (lines.size() > 1 && !lines.get(1).contains("not available")) {
                            buf.append(" ").append(lines.get(1).trim());
                        }
                        return buf.toString();
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to get OpenSSL version", e);
            return null;
        } catch (InterruptedException ignore) {
        }
        logger.warn("Failed to get OpenSSL version: no output");
        return null;
    }
}
