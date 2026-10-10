package com.example.shared.logging;

import com.example.shared.logging.LogPool;
import org.slf4j.Logger;

public final class LogExtensions {
    private LogExtensions() {
    }

    public static void information(Logger logger, LogPool pool, String module, String layer,
                                   String member, String message) {
        logger.info("{}::{} - {}", layer, member, message);
        pool.add(module, layer, "", member, "Information", message, null);
    }

    public static void warning(Logger logger, LogPool pool, String module, String layer,
            String member, String message) {
        logger.warn("{}::{} - {}", layer, member, message);
        pool.add(module, layer, "", member, "Warning", message, null);
    }

    public static void debug(Logger logger, LogPool pool, String module, String layer,
            String member, String message) {
        logger.debug("{}::{} - {}", layer, member, message);
        pool.add(module, layer, "", member, "Debug", message, null);
    }

    public static void error(Logger logger, LogPool pool, String module, String layer,
            String member, String message, Throwable exception) {
        logger.error("{}::{} - {}", layer, member, message, exception);
        pool.add(module, layer, "", member, "Error", message, exception);
    }
}
