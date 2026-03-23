package io.lumine.mythic.lib.script.condition.misc;

import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.script.util.Parsers;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

import java.util.function.Function;

/**
 * Checks if the current world time is DAY/NIGHT/DUSK..
 */
public class TimeCondition extends Condition {
    private final TimePeriod period;

    public static final Function<String, TimePeriod> PARSER_TIME_PERIOD = Parsers.ofEnum(TimePeriod.class, TimePeriod::valueOf);

    public TimeCondition(ConfigObject config) {
        super(config);

        period = config.parse(PARSER_TIME_PERIOD, "period");
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        return period.matches(meta.getSourceLocation().getWorld().getTime());
    }

    public enum TimePeriod {
        DAY(2000, 10000),
        DUSK(14000, 18000),
        NIGHT(14000, 22000),
        DAWN(22000, 2000);

        private final long t1, t2;

        TimePeriod(long t1, long t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        public boolean matches(long time) {
            return t1 < t2 ? t1 <= time && time <= t2 : t1 <= time || time <= t2;
        }
    }
}