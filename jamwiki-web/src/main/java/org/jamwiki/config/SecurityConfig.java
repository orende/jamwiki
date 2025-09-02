package org.jamwiki.config;

import org.apache.commons.lang3.Validate;
import org.jamwiki.authentication.*;
import org.jamwiki.utils.WikiLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import java.util.LinkedHashMap;

import static java.lang.invoke.MethodHandles.lookup;
import static org.jamwiki.authentication.JAMWikiAuthenticationConstants.JAMWIKI_LOGIN_FORM_URL;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final WikiLogger logger = WikiLogger.getLogger(lookup().lookupClass());

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/**/Special:RecentChangesFeed",
                "/**/Special:Setup",
                "/**/*.jsp*",
                "/**/jamwiki.css",
                "/images/**",
                "/js/**",
                "/uploads/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().headers().disable()
                .authorizeRequests()
                .antMatchers("/**/Special:Account").access("hasRole('REGISTER') or isRememberMe() or isAuthenticated()")
                .antMatchers("/**/Special:Admin").hasAuthority("ROLE_SYSADMIN")
                .antMatchers("/**/Special:Block").hasAuthority("ROLE_ADMIN")
                .antMatchers("/**/Special:Edit").hasAnyAuthority("ROLE_EDIT_EXISTING", "ROLE_EDIT_NEW")
                .antMatchers("/**/Special:Import").hasAuthority("ROLE_IMPORT")
                .antMatchers("/**/Special:Login").hasAuthority("ROLE_ANONYMOUS")
                .antMatchers("/**/Special:Maintenance").hasAuthority("ROLE_SYSADMIN")
                .antMatchers("/**/Special:Manage").hasAuthority("ROLE_ADMIN")
                .antMatchers("/**/Special:Move").hasAuthority("ROLE_MOVE")
                .antMatchers("/**/Special:Roles").hasAuthority("ROLE_SYSADMIN")
                .antMatchers("/**/Special:Translation").hasAuthority("ROLE_TRANSLATE")
                .antMatchers("/**/Special:Unblock").hasAuthority("ROLE_ADMIN")
                .antMatchers("/**/Special:Upload").hasAuthority("ROLE_UPLOAD")
                .antMatchers("/**/Special:Upgrade").hasAuthority("ROLE_SYSADMIN")
                .antMatchers("/**/Special:VirtualWiki").hasAuthority("ROLE_SYSADMIN")
                .antMatchers("/**").hasAuthority("ROLE_VIEW")
                .and().logout().logoutSuccessHandler(new JAMWikiLogoutSuccessHandler()).logoutUrl("/j_spring_security_logout")
                .and().rememberMe().key("jam35Wiki").rememberMeCookieName("remember-me").rememberMeServices(rememberMeServices(jamWikiAuthenticationDao()))
                .and().anonymous().key("jam35Wiki")
                .and().addFilterAt(authenticationProcessingFilter(rememberMeServices(jamWikiAuthenticationDao()), authenticationManager(), authenticationFailureHandler()), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jamwikiPostAuthenticationFilter(), ExceptionTranslationFilter.class)
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint(jamwikiErrorMessageProvider()))
                .accessDeniedHandler(jamwikiAccessDeniedHandler(jamwikiErrorMessageProvider()))
                .and().formLogin().loginPage("/**/Special:Login").permitAll();
    }

    @Bean(name="authenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        logger.debug("Entering authenticationManagerBean()");
        AuthenticationManager authenticationManager = super.authenticationManagerBean();
        Validate.notNull(authenticationManager, "authenticationManager cannot be null");
        return authenticationManager;
    }

    @Bean(name = "userDetailsService")
    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception {
        logger.debug("Entering userDetailsServiceBean()");
        UserDetailsService userDetailsService = super.userDetailsServiceBean();
        Validate.notNull(userDetailsService, "userDetailsService cannot be null");
        return userDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        logger.debug("Entering configure((AuthenticationManagerBuilder)");
        auth.authenticationProvider(authenticationProvider(jamwikiPasswordEncoder(),
                jamWikiAuthenticationDao(),
                authoritiesMapper(roleHierarchy())));
    }

    /**
     * This logout success handler redirects to the proper default topic and virtual wiki.
     */
    @Bean
    public JAMWikiLogoutSuccessHandler jamwikiLogoutSuccessHandler() {
        return new JAMWikiLogoutSuccessHandler();
    }

    /**
     * This filter is executed after the user has been authenticated.  It performs two functions:
     *
     * 	1. For users authenticated by LDAP or another system, this filter will create the necessary
     * 	   JAMWiki user database records automatically.
     * 	2. If anonymous users are allowed then this filter will automatically add the roles from the
     * 	   JAMWiki GROUP_ANONYMOUS group.  These roles can be configured through the Special:Roles
     * 	   admin page.  Set the useJAMWikiAnonymousRoles property to false if JAMWiki anonymous
     * 	   roles should not be assigned.
     */
    @Bean
    public JAMWikiPostAuthenticationFilter jamwikiPostAuthenticationFilter() {
        return new JAMWikiPostAuthenticationFilter("jam35Wiki", true);
    }

    /**
     * The error message provider adds a page-specific error message to be used when a user is denied
     * 	access to a page.  For example, a different error is shown to users who are not allowed to edit
     * 	a page than to those who are denied access to the admin pages.
     */
    @Bean
    public JAMWikiErrorMessageProvider jamwikiErrorMessageProvider() {
        var urlPatterns = new LinkedHashMap<String, String>() {{
            put("/**/Special:Account", "login.message.account");
            put("/**/Special:Admin", "login.message.admin");
            put("/**/Special:Edit", "login.message.edit");
            put("/**/Special:Maintenance", "login.message.admin");
            put("/**/Special:Manage", "login.message.admin");
            put("/**/Special:Move", "login.message.move");
            put("/**/Special:Roles", "login.message.admin");
            put("/**/Special:Translation", "login.message.admin");
            put("/**/Special:Upgrade", "login.message.upgrade");
            put("/**/Special:VirtualWiki", "login.message.admin");
            put("/**/*", "login.message.default");
        }};
        return new JAMWikiErrorMessageProvider(urlPatterns);
    }

    /**
     * The entry point is the page to which users are redirected when login is required.
     */
    @Bean
    public JAMWikiAuthenticationProcessingFilterEntryPoint authenticationEntryPoint(JAMWikiErrorMessageProvider errorMessageProvider) {
        Validate.notNull(errorMessageProvider,  "errorMessageProvider cannot be null");
        var authEntryPoint = new JAMWikiAuthenticationProcessingFilterEntryPoint(JAMWIKI_LOGIN_FORM_URL, errorMessageProvider);
        // a PortMapper has to be configured if forceHttps is true and we are not using default ports
        authEntryPoint.setForceHttps(false);
        return authEntryPoint;
    }

    /**
     * This method adds any message from the errorMessageProvider to the redirect.
     */
    @Bean
    public JAMWikiAccessDeniedHandler jamwikiAccessDeniedHandler(JAMWikiErrorMessageProvider errorMessageProvider) {
        Validate.notNull(errorMessageProvider,  "errorMessageProvider cannot be null");
        return new JAMWikiAccessDeniedHandler(errorMessageProvider);
    }

    /**
     * This is necessary to enable using the security:authorize access attribute expressions in jsp,
     * e.g. hasAnyRole().
     */
    @Bean
    public DefaultWebSecurityExpressionHandler webExpressionHandler() {
        return new DefaultWebSecurityExpressionHandler();
    }

    @Bean
    public JAMWikiAuthenticationFailureHandler authenticationFailureHandler() {
        return new JAMWikiAuthenticationFailureHandler("/Special:Login?message=error.login");
    }

    @Bean
    public RememberMeServices rememberMeServices(JAMWikiDaoImpl jamWikiAuthenticationDao) {
        Validate.notNull(jamWikiAuthenticationDao, "jamWikiAuthenticationDao cannot be null");
        return new TokenBasedRememberMeServices("jam35Wiki", jamWikiAuthenticationDao);
    }

    /**
     * The authentication processing filter controls where a user will be sent when he tries to
     * access a protected URL and how that user will be authenticated after trying to login.
     */
    @Bean
    public JAMWikiAuthenticationProcessingFilter authenticationProcessingFilter(RememberMeServices rememberMeServices, AuthenticationManager authenticationManager, AuthenticationFailureHandler authenticationFailureHandler) {
        Validate.notNull(rememberMeServices, "rememberMeServices cannot be null");
        Validate.notNull(authenticationManager, "authenticationManager cannot be null");
        Validate.notNull(authenticationFailureHandler, "authenticationFailureHandler cannot be null");
        var authProcFilter = new JAMWikiAuthenticationProcessingFilter();
        authProcFilter.setAuthenticationManager(authenticationManager);
        authProcFilter.setRememberMeServices(rememberMeServices);
        authProcFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        return authProcFilter;
    }

    /**
     * A temporary class planned to be replaced by Spring Security's SHA password encoder.
     */
    @Bean
    public PasswordEncoder jamwikiPasswordEncoder() {
        return new JAMWikiPasswordEncoder();
    }

    @Bean
    public JAMWikiDaoImpl jamWikiAuthenticationDao() {
        return new JAMWikiDaoImpl();
    }

    @Bean
    public RoleHierarchyImpl roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_SYSADMIN > ROLE_ADMIN > ROLE_USER");
        return roleHierarchy;
    }

    @Bean
    public RoleHierarchyAuthoritiesMapper authoritiesMapper(RoleHierarchyImpl roleHierarchy) {
        Validate.notNull(roleHierarchy, "roleHierarchy cannot be null");
        return new RoleHierarchyAuthoritiesMapper(roleHierarchy);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder jamwikiPasswordEncoder, JAMWikiDaoImpl jamWikiAuthenticationDao, RoleHierarchyAuthoritiesMapper authoritiesMapper) {
        Validate.notNull(jamwikiPasswordEncoder, "jamwikiPasswordEncoder cannot be null");
        Validate.notNull(jamWikiAuthenticationDao, "jamWikiAuthenticationDao cannot be null");
        Validate.notNull(authoritiesMapper, "authoritiesMapper cannot be null");
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(jamwikiPasswordEncoder);
        authProvider.setUserDetailsService(jamWikiAuthenticationDao);
        authProvider.setAuthoritiesMapper(authoritiesMapper);
        return authProvider;
    }
}
