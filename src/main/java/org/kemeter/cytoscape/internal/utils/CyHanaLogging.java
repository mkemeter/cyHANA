package org.kemeter.cytoscape.internal.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyHanaLogging {

    // private static Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");
    private static Logger logger = LoggerFactory.getLogger(CyHanaLogging.class);

    public static void debug(String msg){
        logger.debug("[cyHANA] " + msg);
    }

    public static void info(String msg){
        logger.info( "[cyHANA] " + msg);
    }

    public static void warn(String msg){
        logger.warn( "[cyHANA] " + msg);
    }

    public static void err(String msg){
        logger.error("[cyHANA] " + msg);
    }
}
