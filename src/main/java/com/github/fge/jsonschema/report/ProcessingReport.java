/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.fge.jsonschema.report;

import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;

import java.util.Iterator;

/**
 * Base implementation of a processing report
 *
 * <p>This abstract class implements all the logic of a processing report. The
 * only method you need to implement is {@link
 * #log(LogLevel, ProcessingMessage)}, which will implement the actual logging
 * of the message. When entering this method, the message's log level will
 * already have been set correctly.</p>
 */
public abstract class ProcessingReport
    implements MessageProvider, Iterable<ProcessingMessage>
{
    @VisibleForTesting
    protected LogLevel currentLevel = LogLevel.DEBUG;
    private final LogLevel logLevel;
    private final LogLevel exceptionThreshold;

    protected ProcessingReport(final LogLevel logLevel,
        final LogLevel exceptionThreshold)
    {
        this.logLevel = logLevel;
        this.exceptionThreshold = exceptionThreshold;
    }

    protected ProcessingReport(final LogLevel logLevel)
    {
        this(logLevel, LogLevel.FATAL);
    }

    protected ProcessingReport()
    {
        this(LogLevel.INFO, LogLevel.FATAL);
    }

    public final LogLevel getLogLevel()
    {
        return logLevel;
    }

    public final LogLevel getExceptionThreshold()
    {
        return exceptionThreshold;
    }

    public final void debug(final ProcessingMessage message)
        throws ProcessingException
    {
        dispatch(message.setLogLevel(LogLevel.DEBUG));
    }

    public final void info(final ProcessingMessage message)
        throws ProcessingException
    {
        dispatch(message.setLogLevel(LogLevel.INFO));
    }

    public final void warn(final ProcessingMessage message)
        throws ProcessingException
    {
        dispatch(message.setLogLevel(LogLevel.WARNING));
    }

    public final void error(final ProcessingMessage message)
        throws ProcessingException
    {
        dispatch(message.setLogLevel(LogLevel.ERROR));
    }

    public final boolean isSuccess()
    {
        return currentLevel.compareTo(LogLevel.ERROR) < 0;
    }

    public abstract void log(final LogLevel level,
        final ProcessingMessage message);

    private void dispatch(final ProcessingMessage message)
        throws ProcessingException
    {
        final LogLevel level = message.getLogLevel();

        if (level.compareTo(exceptionThreshold) >= 0)
            throw message.asException();
        if (level.compareTo(currentLevel) > 0)
            currentLevel = level;
        if (level.compareTo(logLevel) >= 0)
            log(level, message.setLogLevel(level));
    }

    @Override
    public Iterator<ProcessingMessage> iterator()
    {
        return Iterators.emptyIterator();
    }

    @Override
    public final ProcessingMessage newMessage()
    {
        return new ProcessingMessage();
    }

    public final void mergeWith(final ProcessingReport other)
        throws ProcessingException
    {
        for (final ProcessingMessage message: other)
            dispatch(message);
    }
}
