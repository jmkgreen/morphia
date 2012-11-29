package com.github.jmkgreen.morphia.logging.slf4j;

import org.slf4j.Logger;
import com.github.jmkgreen.morphia.logging.Logr;

/**
 *
 */
public class SLF4JLogr implements Logr {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    private final Logger logger;

    /**
     *
     * @param c
     */
    public SLF4JLogr(final Class<?> c) {
        this.logger = org.slf4j.LoggerFactory.getLogger(c);
    }

    /**
     *
     * @return
     */
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }

    /**
     *
     * @param msg
     */
    public void trace(final String msg) {
        this.logger.trace(msg);
    }

    /**
     *
     * @param format
     * @param argArray
     */
    public void trace(final String format, final Object... argArray) {
        this.logger.trace(format, argArray);
    }

    /**
     *
     * @param msg
     * @param t
     */
    public void trace(final String msg, final Throwable t) {
        this.logger.trace(msg, t);
    }

    /**
     *
     * @return
     */
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    /**
     *
     * @param msg
     */
    public void debug(final String msg) {
        this.logger.debug(msg);
    }

    /**
     *
     * @param format
     * @param argArray
     */
    public void debug(final String format, final Object... argArray) {
        this.logger.debug(format, argArray);
    }

    /**
     *
     * @param msg
     * @param t
     */
    public void debug(final String msg, final Throwable t) {
        this.logger.debug(msg, t);
    }

    /**
     *
     * @return
     */
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    /**
     *
     * @param msg
     */
    public void info(final String msg) {
        this.logger.info(msg);
    }

    /**
     *
     * @param format
     * @param argArray
     */
    public void info(final String format, final Object... argArray) {
        this.logger.info(format, argArray);
    }

    /**
     *
     * @param msg
     * @param t
     */
    public void info(final String msg, final Throwable t) {
        this.logger.info(msg, t);
    }

    /**
     *
     * @return
     */
    public boolean isWarningEnabled() {
        return this.logger.isWarnEnabled();
    }

    /**
     *
     * @param msg
     */
    public void warning(final String msg) {
        this.logger.warn(msg);
    }

    /**
     *
     * @param format
     * @param argArray
     */
    public void warning(final String format, final Object... argArray) {
        this.logger.warn(format, argArray);
    }

    /**
     *
     * @param msg
     * @param t
     */
    public void warning(final String msg, final Throwable t) {
        this.logger.warn(msg, t);
    }

    /**
     *
     * @return
     */
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    /**
     *
     * @param msg
     */
    public void error(final String msg) {
        this.logger.error(msg);
    }

    /**
     *
     * @param format
     * @param argArray
     */
    public void error(final String format, final Object... argArray) {
        this.logger.error(format, argArray);
    }

    /**
     *
     * @param msg
     * @param t
     */
    public void error(final String msg, final Throwable t) {
        this.logger.error(msg, t);
    }

}
