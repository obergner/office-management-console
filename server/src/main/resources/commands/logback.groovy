import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.Appender
import org.crsh.cli.*
import org.crsh.cli.completers.EnumCompleter
import org.crsh.cli.descriptor.ParameterDescriptor
import org.crsh.cli.spi.Completer
import org.crsh.cli.spi.Completion
import org.crsh.command.InvocationContext
import org.crsh.command.Pipe
import org.crsh.text.Color
import org.slf4j.LoggerFactory

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.regex.Pattern

@Usage("Configure logback logging backend")
public class logback {

    static Collection<String> getLoggers() {
        def names = [] as Set;

        // Logback
        LoggerContext lc = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        List<Logger> allLoggers = lc.loggerList;

        // Add the known names
        for (Logger logger : allLoggers) {
            if (logger.level != null /*&& hasAppenders(logger)*/) {
                names.addAll(logger.name);
            }
        }

        //
        return names;
    }

    static boolean hasAppenders(Logger logger) {
        Iterator<Appender<LoggingEvent>> it = logger.iteratorForAppenders();
        return it.hasNext();
    }

    @Usage("send a message to a logback logger")
    @Man("""\
The send command log one or several loggers with a specified message. For instance the
following impersonates the javax.management.mbeanserver class and send a message on its own
logger.
#% logback send -m hello javax.management.mbeanserver
Send is a <Logger, Void> command, it can log messages to consumed log objects:
% logback ls | logback send -m hello -l warn""")
    @Command
    public Pipe<Logger, Object> send(@MsgOpt String msg, @LoggerArg String name, @LevelOpt Level level) {
        level = level ?: Level.info;
        return new Pipe<Logger, Object>() {
            @Override
            void open() {
                if (name != null) {
                    def logger = LoggerFactory.getLogger(name);
                    level.log(logger, msg);
                }
            }

            @Override
            void provide(Logger element) {
                level.log(element, msg);
            }
        }
    }


    @Usage("list the available loggers")
    @Man("""\
The logback ls command list all the available loggers, for instance:
% logback ls
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/].[default]
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/eXoGadgetServer].[concat]
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/dashboard].[jsp]
...
The -f switch provides filtering with a Java regular expression
% logback ls -f javax.*
javax.management.mbeanserver
javax.management.modelmbean
The logback ls command is a <Void,Logger> command, therefore any logger produced can be
consumed.""")
    @Command
    public void ls(InvocationContext<Logger> context, @FilterOpt String filter) {

        // Regex filter
        def pattern = Pattern.compile(filter ?: ".*");

        //
        loggers.each {
            def matcher = it =~ pattern;
            if (matcher.matches()) {
                def logger = LoggerFactory.getLogger(it);
                context.provide(logger);
            }
        }
    }

    @Usage("create one or several loggers")
    @Command
    public void add(InvocationContext<Logger> context, @LoggerArg List<String> names) {
        names.each {
            if (it.length() > 0) {
                Logger logger = LoggerFactory.getLogger(it);
                if (logger != null) {
                    context.provide(logger);
                }
            }
        }
    }

    @Man("""\
The set command sets the level of a logger. One or several logger names can be specified as
arguments and the -l option specify the level among the trace, debug, fine, info, warn and
error levels. When no level is specified, the level is cleared and the level will be
inherited from its ancestors.
% logback set -l trace foo
% logback set foo
The logger name can be omitted and instead stream of logger can be consumed as it is a
<Logger,Void> command. The following set the level warn on all the available loggers:
% logback ls | logback set -l warn""")
    @Usage("configures the level of one of several loggers")
    @Command
    public Pipe<Logger, Object> set(@LoggerArg List<String> names, @LevelOpt @Required Level level) {

        //
        return new Pipe<Logger, Object>() {
            @Override
            void open() {
                names.each() {
                    def logger = LoggerFactory.getLogger(it);
                    level.setLevel(logger)
                }
            }

            @Override
            void provide(Logger element) {
                level.setLevel(element);
            }
        };
    }
}

enum Level {
    trace(ch.qos.logback.classic.Level.TRACE, Color.blue),
    debug(ch.qos.logback.classic.Level.DEBUG, Color.blue),
    info(ch.qos.logback.classic.Level.INFO, Color.white),
    warning(ch.qos.logback.classic.Level.WARN, Color.yellow),
    error(ch.qos.logback.classic.Level.ERROR, Color.red);

    static Level valueOf(ch.qos.logback.classic.Level level) {
        switch (level.intValue()) {
            case ch.qos.logback.classic.Level.TRACE_INT:
                return trace;
            case ch.qos.logback.classic.Level.DEBUG_INT:
                return debug;
            case ch.qos.logback.classic.Level.INFO_INT:
                return info;
            case ch.qos.logback.classic.Level.WARN_INT:
                return warning;
            case ch.qos.logback.classic.Level.ERROR_INT:
                return error;
            default:
                return null;
        }
    }

    final Color color;
    final ch.qos.logback.classic.Level value;

    Level(ch.qos.logback.classic.Level value, Color color) {
        this.value = value;
        this.color = color;
    }

    void log(Logger logger, String msg) {
        switch (this.value) {
            case ch.qos.logback.classic.Level.TRACE:
                logger.trace(msg);
                break;
            case ch.qos.logback.classic.Level.DEBUG:
                logger.debug(msg);
                break;
            case ch.qos.logback.classic.Level.INFO:
                logger.info(msg);
                break;
            case ch.qos.logback.classic.Level.WARN:
                logger.warn(msg);
                break;
            case ch.qos.logback.classic.Level.ERROR:
                logger.error(msg);
                break;
            default:
                logger.error("Unknown log level: {}", this.value);
        }
    }

    void setLevel(Logger logger) {
        logger.level = value;
    }
}

class LoggerCompleter implements Completer {

    Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
        def builder = new Completion.Builder(prefix);
        logback.loggers.each() {
            if (it.startsWith(prefix)) {
                builder.add(it.substring(prefix.length()), true);
            }
        }
        return builder.build();
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Usage("the logger level")
@Man("The logger level to assign among {trace, debug, info, warn, error}")
@Option(names = ["l", "level"], completer = EnumCompleter)
@interface LevelOpt {}

@Retention(RetentionPolicy.RUNTIME)
@Usage("the message")
@Man("The message to log")
@Option(names = ["m", "message"])
@Required
@interface MsgOpt {}

@Retention(RetentionPolicy.RUNTIME)
@Usage("the logger name")
@Man("The name of the logger")
@Argument(name = "name", completer = LoggerCompleter.class)
@interface LoggerArg {}

@Retention(RetentionPolicy.RUNTIME)
@Usage("a regexp filter")
@Man("A regular expressions used to filter the loggers")
@Option(names = ["f", "filter"])
@interface FilterOpt {}
