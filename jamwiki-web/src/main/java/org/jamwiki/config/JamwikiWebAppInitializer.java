package org.jamwiki.config;

import org.jamwiki.servlets.JAMWikiFilter;
import org.jamwiki.servlets.JAMWikiListener;
import org.jamwiki.utils.WikiLogger;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import static java.lang.invoke.MethodHandles.lookup;

public class JamwikiWebAppInitializer implements WebApplicationInitializer {
    private static final WikiLogger logger = WikiLogger.getLogger(lookup().lookupClass());

    @Override
    public void onStartup(ServletContext container) {
        logger.debug("Entering JAMWikiWebAppInitializer.onStartup");
        // TODO can this be replaced with @ComponentScan?
        // Create the 'root' Spring application context
        AnnotationConfigWebApplicationContext rootContext =
                new AnnotationConfigWebApplicationContext();
        rootContext.register(AppConfig.class);

        rootContext.register(SecurityConfig.class);

        // Manage the lifecycle of the root application context
        container.addListener(new ContextLoaderListener(rootContext));

        var filterRegistration = container.addFilter("JAMWikiFilter", JAMWikiFilter.class);
        boolean setResult = filterRegistration.setInitParameter("encoding", "UTF-8");
        logger.debug("Init parameter encoding set on filter reg result: " + setResult);
        var secFilterRegistration = container.addFilter("springSecurityFilterChain", DelegatingFilterProxy.class);

        filterRegistration.addMappingForUrlPatterns(null, true, "/*");
        secFilterRegistration.addMappingForUrlPatterns(null, true, "/*");

        container.addListener(new JAMWikiListener());

        // Create the dispatcher servlet's Spring application context
        AnnotationConfigWebApplicationContext dispatcherContext =
                new AnnotationConfigWebApplicationContext();
        dispatcherContext.register(DispatcherConfig.class);

        // Register and map the dispatcher servlet
        DispatcherServlet jamwikiServlet = new DispatcherServlet(dispatcherContext);
        ServletRegistration.Dynamic dispatcher =
                container.addServlet("jamwiki", jamwikiServlet);
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/en/*");
        dispatcher.addMapping("/uploads/*");
    }
}