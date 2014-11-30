package io.obergner.office.shell;

import ch.qos.logback.classic.Logger;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.LineRenderer;
import org.crsh.text.Renderer;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;

import java.util.Iterator;

public class LogbackLoggerRenderer extends Renderer<Logger> {

    @Override
    public Class<Logger> getType() {
        return Logger.class;
    }

    @Override
    public LineRenderer renderer(final Iterator<Logger> stream) {
        final TableElement table = new TableElement();

        // Header
        table.add(new RowElement().style(Decoration.bold.fg(Color.black).bg(Color.white)).add("NAME", "LEVEL"));

        while (stream.hasNext()) {
            final Logger logger = stream.next();

            // Determine level
            final String level;
            if (logger.isTraceEnabled()) {
                level = "TRACE";
            } else if (logger.isDebugEnabled()) {
                level = "DEBUG";
            } else if (logger.isInfoEnabled()) {
                level = "INFO";
            } else if (logger.isWarnEnabled()) {
                level = "WARN";
            } else if (logger.isErrorEnabled()) {
                level = "ERROR";
            } else {
                level = "UNKNOWN";
            }

            table.row(logger.getName(), level);
        }

        return table.renderer();
    }
}
