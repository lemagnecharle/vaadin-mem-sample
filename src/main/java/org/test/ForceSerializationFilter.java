package org.test;


import org.apache.commons.lang3.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listener zum erzwingen von Session-Serialisierung nach jedem Aufruf.
 * Dadurch kann getestet werden, ob die Applikation in geklusterten Umgebungen richtig funktioniert.
 *
 */
@WebFilter(urlPatterns = "*")
public class ForceSerializationFilter implements Filter {

    private static final Logger logger = Logger.getLogger(ForceSerializationFilter.class.getName());


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to do
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        }
        finally {
            logSession((HttpServletRequest) request);
        }
    }

    @Override
    public void destroy() {
        // nix zu tun
    }

    public void logSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);  // don't create
        if (session != null && isPageRequest(request)) {
            long bytes = 0;

            Enumeration<String> names = session.getAttributeNames();
            // each session attribute
            for (String name : Collections.list(names)) {

                // force each session attribute through "serialization"
                try {
                    // 1. read
                    Object object = session.getAttribute(name);

                    // 2. serialize
                    int size = serialize(object).length;
                    logger.log(Level.INFO, "Attribute {} size={}", new Object[]{name, size});

                    bytes += size;

                }
                catch (Exception e) {
                    logger.log(Level.SEVERE, "serialization failed!. attribute={}.\nCaused By:\n{}",
                            new Object[]{name, e.getMessage()});
                }
            }
            logger.info("serialized session with id=" + session.getId() + " size=" + bytes + " bytes");
        }
    }

    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DebuggingObjectOutputStream oos = new DebuggingObjectOutputStream(bos);
        try {
            oos.writeObject(object);
        }
        catch (IOException e) {
            List<Object> o = oos.getStack();

            StringBuilder fancyText = new StringBuilder();
            fancyText.append("\n\n\n")
                    .append("            o                                         o                                         o           \n")
                    .append("           ooo                                       ooo                                       ooo          \n")
                    .append("          ooooo                                     ooooo                                     ooooo         \n")
                    .append("         ooooooo                                   ooooooo                                   ooooooo        \n")
                    .append("        ooooooooo                                 ooooooooo                                 ooooooooo       \n")
                    .append("           ooo                                       ooo                                       ooo          \n")
                    .append("           ooo                                       ooo                                       ooo          \n")
                    .append("           ooo                                       ooo                                       ooo          \n")
                    .append("           ooo                                       ooo                                       ooo          \n")
                    .append("   _____           _       _ _          _   _               ______                    _   _               _ \n")
                    .append("  / ____|         (_)     | (_)        | | (_)             |  ____|                  | | (_)             | |\n")
                    .append(" | (___   ___ _ __ _  __ _| |_ ______ _| |_ _  ___  _ __   | |__  __  _____ ___ _ __ | |_ _  ___  _ __   | |\n")
                    .append("  \\___ \\ / _ \\ '__| |/ _` | | |_  / _` | __| |/ _ \\| '_ \\  |  __| \\ \\/ / __/ _ \\ '_ \\| __| |/ _ \\| '_ \\  | |\n")
                    .append("  ____) |  __/ |  | | (_| | | |/ / (_| | |_| | (_) | | | | | |____ >  < (_|  __/ |_) | |_| | (_) | | | | |_|\n")
                    .append(" |_____/ \\___|_|  |_|\\__,_|_|_/___\\__,_|\\__|_|\\___/|_| |_| |______/_/\\_\\___\\___| .__/ \\__|_|\\___/|_| |_| (_)\n")
                    .append("                                                                               | |                          \n")
                    .append("                                                                               |_|                          \n");

            throw new IOException("Exception while serializing:\n*** " + StringUtils.join(o, "\n*** ") + fancyText);
        }

        byte[] objectBytes = bos.toByteArray();
        oos.close();
        bos.close();

        return objectBytes;
    }

    private boolean isPageRequest(HttpServletRequest request) {
        return request.getRequestURI().contains("UIDL");
    }
}
