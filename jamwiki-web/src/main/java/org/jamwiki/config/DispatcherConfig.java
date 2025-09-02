package org.jamwiki.config;

import org.jamwiki.servlets.JAMWikiLocaleInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.util.HashMap;
import java.util.Map;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = "org.jamwiki.servlets")
public class DispatcherConfig extends WebMvcConfigurerAdapter {

    @Bean
    public SimpleUrlHandlerMapping urlMappingDefault() {
        var handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(2);
        handlerMapping.setAlwaysUseFullPath(true);
        handlerMapping.setInterceptors(new JAMWikiLocaleInterceptor());
        handlerMapping.setUrlMap(Map.of("/*", "Topic"));
        return handlerMapping;
    }

    @Bean
    public SimpleUrlHandlerMapping urlMapping() {
        var handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setAlwaysUseFullPath(true);
        handlerMapping.setInterceptors(new JAMWikiLocaleInterceptor());
        handlerMapping.setUrlMap(new HashMap<>() {{
            put("/**/Special:Account", "Register");
            put("/**/Special:Admin", "Admin");
            put("/**/Special:AllPages", "Items");
            put("/**/Special:Block", "Block");
            put("/**/Special:BlockList", "BlockList");
            put("/**/Special:Categories", "Category");
            put("/**/Special:Contributions", "Contributions");
            put("/**/Special:Diff", "Diff");
            put("/**/Special:Edit", "Edit");
            put("/**/Special:Export", "Export");
            put("/**/Special:FileList", "Items");
            put("/**/Special:History", "History");
            put("/**/Special:Import", "Import");
            put("/**/Special:ImportTiddly", "ImportTiddly");
            put("/**/Special:ImageList", "Items");
            put("/**/Special:LinkTo", "LinkTo");
            put("/**/Special:ListUsers", "Items");
            put("/**/Special:Login", "Login");
            put("/**/Special:Logout", "Login");
            put("/**/Special:Log", "Log");
            put("/**/Special:Logs", "Log");
            put("/**/Special:Maintenance", "Admin");
            put("/**/Special:Manage", "Manage");
            put("/**/Special:Move", "Move");
            put("/**/Special:OrphanedPages", "Items");
            put("/**/Special:PasswordReset", "PasswordReset");
            put("/**/Special:Print", "Printable");
            put("/**/Special:RecentChanges", "RecentChanges");
            put("/**/Special:RecentChangesFeed", "RecentChangesFeed");
            put("/**/Special:Roles", "Roles");
            put("/**/Special:Search", "Search");
            put("/**/Special:Setup", "Setup");
            put("/**/Special:Source", "ViewSource");
            put("/**/Special:SpecialPages", "SpecialPages");
            put("/**/Special:TopicsAdmin", "Items");
            put("/**/Special:Translation", "Translation");
            put("/**/Special:Unblock", "Block");
            put("/**/Special:Upgrade", "Upgrade");
            put("/**/Special:Upload", "Upload");
            put("/**/Special:VirtualWiki", "VirtualWiki");
            put("/**/Special:Watchlist", "Watchlist");
            put("/**/jamwiki.css", "Stylesheet");
            put("/uploads/**/*", "Image");
        }});
        return handlerMapping;
    }

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        viewResolver.setViewClass(JstlView.class);
        return viewResolver;
    }

    @Bean
    public SessionLocaleResolver localeResolver() {
        return new SessionLocaleResolver();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.jsp");
    }
}
