package org.intrigger.ultimate_market.output;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Bukkit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a filter for Log4j to skip sensitive AuthMe commands.
 *
 * @author Xephi59
 */
public class Log4JFilter extends AbstractFilter implements Serializable {

    private static final long serialVersionUID = -5594073755007974254L;

    /**
     * Validates a Message instance and returns the {@link Result} value
     * depending on whether the message contains sensitive AuthMe data.
     *
     * @param message The Message object to verify
     *
     * @return The Result value
     */
    private static Result validateMessage(Message message) {
        if (message == null) {
            return Result.NEUTRAL;
        }
        return validateMessage(message.getFormattedMessage());
    }

    /**
     * Validates a message and returns the {@link Result} value depending
     * on whether the message contains sensitive AuthMe data.
     *
     * @param message The message to verify
     *
     * @return The Result value
     */
    private static Result validateMessage(String message) {
        List<String> strings = new ArrayList<>(Bukkit.getPluginCommand("ah").getAliases());
        strings.add("ah");
        for (String s: strings){
            if (message.contains("issued server command: /" + s)) return Result.DENY;
        }
        return Result.ACCEPT;
    }

    @Override
    public Result filter(LogEvent event) {
        Message candidate = null;
        if (event != null) {
            candidate = event.getMessage();
        }
        return validateMessage(candidate);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return validateMessage(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return validateMessage(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        String candidate = null;
        if (msg != null) {
            candidate = msg.toString();
        }
        return validateMessage(candidate);
    }
}
