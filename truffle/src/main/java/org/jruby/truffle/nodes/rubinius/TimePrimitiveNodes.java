/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.rubinius;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jruby.truffle.nodes.objectstorage.ReadHeadObjectFieldNode;
import org.jruby.truffle.nodes.objectstorage.WriteHeadObjectFieldNode;
import org.jruby.truffle.runtime.DebugOperations;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.*;
import org.jruby.util.RubyDateFormatter;

/**
 * Rubinius primitives associated with the Ruby {@code Time} class.
 * <p>
 * Also see {@link RubyTime}.
 */
public abstract class TimePrimitiveNodes {

    @RubiniusPrimitive(name = "time_s_now")
    public static abstract class TimeSNowPrimitiveNode extends RubiniusPrimitiveNode {

        public TimeSNowPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimeSNowPrimitiveNode(TimeSNowPrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public RubyTime timeSNow(RubyClass timeClass) {
            // TODO CS 14-Feb-15 uses debug send
            final DateTimeZone zone = org.jruby.RubyTime.getTimeZoneFromTZString(getContext().getRuntime(),
                    DebugOperations.send(getContext(), getContext().getCoreLibrary().getENV(), "[]", null, getContext().makeString("TZ")).toString());
            return new RubyTime(timeClass, DateTime.now(zone));
        }

    }

    @RubiniusPrimitive(name = "time_s_dup", needsSelf = false)
    public static abstract class TimeSDupPrimitiveNode extends RubiniusPrimitiveNode {

        public TimeSDupPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimeSDupPrimitiveNode(TimeSDupPrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public RubyTime timeSDup(RubyTime other) {
            final RubyTime time = new RubyTime(getContext().getCoreLibrary().getTimeClass(), other.getDateTime());
            return time;
        }

    }

    @RubiniusPrimitive(name = "time_s_specific", needsSelf = false)
    public static abstract class TimeSSpecificPrimitiveNode extends RubiniusPrimitiveNode {

        public TimeSSpecificPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimeSSpecificPrimitiveNode(TimeSSpecificPrimitiveNode prev) {
            super(prev);
        }

        @Specialization(guards = "isTrue(arguments[2])")
        public RubyTime timeSSpecificUTC(int seconds, int nanoseconds, boolean isUTC, RubyNilClass offset) {
            // TODO(CS): overflow checks needed?
            final long milliseconds = seconds * 1_000 + (nanoseconds / 1_000_000);
            return new RubyTime(getContext().getCoreLibrary().getTimeClass(), new DateTime(milliseconds));
        }

    }

    @RubiniusPrimitive(name = "time_seconds")
    public static abstract class TimeSecondsPrimitiveNode extends RubiniusPrimitiveNode {

        public TimeSecondsPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimeSecondsPrimitiveNode(TimeSecondsPrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public long timeSeconds(RubyTime time) {
            return time.getDateTime().getMillis() / 1_000;
        }

    }

    @RubiniusPrimitive(name = "time_useconds")
    public static abstract class TimeUSecondsPrimitiveNode extends RubiniusPrimitiveNode {

        public TimeUSecondsPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimeUSecondsPrimitiveNode(TimeUSecondsPrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public long timeUSeconds(RubyTime time) {
            return time.getDateTime().getMillisOfSecond() * 1_000;
        }

    }

    @RubiniusPrimitive(name = "time_decompose")
    public static abstract class TimeDecomposePrimitiveNode extends RubiniusPrimitiveNode {

        public TimeDecomposePrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimeDecomposePrimitiveNode(TimeDecomposePrimitiveNode prev) {
            super(prev);
        }

        @CompilerDirectives.TruffleBoundary
        @Specialization
        public RubyArray timeDecompose(RubyTime time) {
            final DateTime dateTime = time.getDateTime();
            final int sec = dateTime.getSecondOfMinute();
            final int min = dateTime.getMinuteOfHour();
            final int hour = dateTime.getHourOfDay();
            final int day = dateTime.getDayOfMonth();
            final int month = dateTime.getMonthOfYear();
            final int year = dateTime.getYear();
            final int wday = dateTime.getDayOfWeek();
            final int yday = dateTime.getDayOfYear();
            final boolean isdst = false;

            // TODO CS 14-Feb-15 uses debug send
            final String envTimeZoneString = DebugOperations.send(getContext(), getContext().getCoreLibrary().getENV(), "[]", null, getContext().makeString("TZ")).toString();
            String zoneString = org.jruby.RubyTime.zoneHelper(envTimeZoneString, dateTime, false);
            Object zone;
            if (zoneString.matches(".*-\\d+")) {
                zone = getContext().getCoreLibrary().getNilObject();
            } else {
                zone = getContext().makeString(zoneString);
            }

            final Object[] decomposed = new Object[]{sec, min, hour, day, month, year, wday, yday, isdst, zone};
            return new RubyArray(getContext().getCoreLibrary().getArrayClass(), decomposed, decomposed.length);
        }

    }

    @RubiniusPrimitive(name = "time_strftime")
    public static abstract class TimeStrftimePrimitiveNode extends RubiniusPrimitiveNode {

        public TimeStrftimePrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimeStrftimePrimitiveNode(TimeStrftimePrimitiveNode prev) {
            super(prev);
        }

        @CompilerDirectives.TruffleBoundary
        @Specialization
        public RubyString timeStrftime(RubyTime time, RubyString format) {
            final RubyDateFormatter rdf = getContext().getRuntime().getCurrentContext().getRubyDateFormatter();
            // TODO CS 15-Feb-15 ok to just pass nanoseconds as 0?
            return getContext().makeString(rdf.formatToByteList(rdf.compilePattern(format.getBytes(), false), time.getDateTime(), 0, null));
        }

    }

    @RubiniusPrimitive(name = "time_s_from_array", needsSelf = false)
    public static abstract class TimeSFromArrayPrimitiveNode extends RubiniusPrimitiveNode {

        public TimeSFromArrayPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimeSFromArrayPrimitiveNode(TimeSFromArrayPrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public RubyTime timeSFromArray(int sec, int min, int hour, int mday, int month, int year,
                                       RubyNilClass nsec, int isdst, boolean fromutc, Object utcoffset) {
            if (isdst == -1 && !fromutc && utcoffset instanceof Integer) {
                final DateTime dateTime = new DateTime(year, month, mday, hour, min, sec, DateTimeZone.forOffsetMillis(((int) utcoffset) * 1_000));
                return new RubyTime(getContext().getCoreLibrary().getTimeClass(), dateTime);
            } else {
                throw new UnsupportedOperationException(String.format("%s %s %s", isdst, fromutc, utcoffset));
            }
        }

    }

    @RubiniusPrimitive(name = "time_nseconds")
    public static abstract class TimeNSecondsPrimitiveNode extends RubiniusPrimitiveNode {

        public TimeNSecondsPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimeNSecondsPrimitiveNode(TimeNSecondsPrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public long timeNSeconds(RubyTime time) {
            return time.getDateTime().getMillisOfSecond() * 1_000_000;
        }

    }

    @RubiniusPrimitive(name = "time_set_nseconds")
    public static abstract class TimeSetNSecondsPrimitiveNode extends RubiniusPrimitiveNode {

        public TimeSetNSecondsPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimeSetNSecondsPrimitiveNode(TimeSetNSecondsPrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public long timeSetNSeconds(RubyTime time, int nanoseconds) {
            time.setDateTime(time.getDateTime().withMillisOfSecond(nanoseconds / 1_000_000));
            return nanoseconds;
        }

    }

    @RubiniusPrimitive(name = "time_env_zone")
    public static abstract class TimeEnvZonePrimitiveNode extends RubiniusPrimitiveNode {

        public TimeEnvZonePrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimeEnvZonePrimitiveNode(TimeEnvZonePrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public Object timeEnvZone(RubyTime time) {
            throw new UnsupportedOperationException("time_env_zone");
        }

    }

    @RubiniusPrimitive(name = "time_utc_offset")
    public static abstract class TimeUTCOffsetPrimitiveNode extends RubiniusPrimitiveNode {

        public TimeUTCOffsetPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimeUTCOffsetPrimitiveNode(TimeUTCOffsetPrimitiveNode prev) {
            super(prev);
        }

        @Specialization
        public Object timeUTCOffset(RubyTime time) {
            throw new UnsupportedOperationException("time_utc_offset");
        }

    }

}