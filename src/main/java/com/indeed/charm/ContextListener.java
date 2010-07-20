package com.indeed.charm;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionBindingEvent;

import com.indeed.charm.svn.SubversionClient;
import com.indeed.charm.actions.BackgroundJobManager;
import com.google.common.collect.MapMaker;

/**
 */
public class ContextListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    private static Logger log = Logger.getLogger(ContextListener.class);

    // Public constructor is required by servlet spec
    public ContextListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
        Logger.getLogger("com.indeed.charm").setLevel(Level.INFO);
        try {
            final ServletContext ctx = sce.getServletContext();
            final ReleaseEnvironment env = new ReleaseEnvironment(sce.getServletContext());
            ctx.setAttribute(ReleaseEnvironment.class.getSimpleName(), env);
            // TODO: select VCS client based on charm.properties
            final VCSClient svnClient = new SubversionClient(env);
            ctx.setAttribute(VCSClient.class.getSimpleName(), svnClient);
            final ProjectsLoader projectsLoader = new ProjectsLoader(env, svnClient);
            ctx.setAttribute(ProjectsLoader.class.getSimpleName(), projectsLoader);
            final BackgroundJobManager backgroundJobManager = new BackgroundJobManager();
            ctx.setAttribute(BackgroundJobManager.class.getSimpleName(), backgroundJobManager);
            ctx.setAttribute("DepGraphCache", new MapMaker().softValues().makeMap());
        } catch (VCSException e) {
            log.error("Unable to initialize subversion", e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        /* This method is invoked when the Servlet Context 
           (the Web application) is undeployed or 
           Application Server shuts down.
        */
    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    public void sessionCreated(HttpSessionEvent se) {
        /* Session is created. */
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        /* Session is destroyed. */
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded(HttpSessionBindingEvent sbe) {
        /* This method is called when an attribute 
           is added to a session.
        */
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
        /* This method is called when an attribute
           is removed from a session.
        */
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
        /* This method is invoked when an attibute
           is replaced in a session.
        */
    }
}