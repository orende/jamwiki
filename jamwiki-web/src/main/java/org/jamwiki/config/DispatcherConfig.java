package org.jamwiki.config;

import org.jamwiki.servlets.*;
import org.springframework.context.annotation.Bean;
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
public class DispatcherConfig extends WebMvcConfigurerAdapter {

    // TODO replace these builder methods with @ComponentScan and annotate all servlets with @Component("beanName")

    @Bean(name = "Admin")
    public AdminServlet Admin() {
        return new AdminServlet();
    }

    @Bean(name = "VirtualWiki")
    public AdminVirtualWikiServlet VirtualWiki() {
        return new AdminVirtualWikiServlet();
    }

    @Bean(name = "BlockList")
    public BlockListServlet BlockList() {
        return new BlockListServlet();
    }

    @Bean(name = "Block")
    public BlockServlet Block() {
        return new BlockServlet();
    }

    @Bean(name = "Category")
    public CategoryServlet Category() {
        return new CategoryServlet();
    }

    @Bean(name = "Contributions")
    public ContributionsServlet Contributions() {
        return new ContributionsServlet();
    }

    @Bean(name = "Diff")
    public DiffServlet Diff() {
        return new DiffServlet();
    }

    @Bean(name = "Edit")
    public EditServlet Edit() {
        return new EditServlet();
    }

    @Bean(name = "Export")
    public ExportServlet Export() {
        return new ExportServlet();
    }

    @Bean(name = "History")
    public HistoryServlet History() {
        return new HistoryServlet();
    }

    @Bean(name = "Image")
    public ImageServlet Image() {
        return new ImageServlet();
    }

    @Bean(name = "Import")
    public ImportServlet Import() {
        return new ImportServlet();
    }

    @Bean(name = "ImportTiddly")
    public ImportTiddlyWikiServlet ImportTiddly() {
        return new ImportTiddlyWikiServlet();
    }

    @Bean(name = "Items")
    public ItemsServlet Items() {
        return new ItemsServlet();
    }

    @Bean(name = "LinkTo")
    public LinkToServlet LinkTo() {
        return new LinkToServlet();
    }

    @Bean(name = "Login")
    public LoginServlet Login() {
        return new LoginServlet();
    }

    @Bean(name = "Log")
    public LogServlet Log() {
        return new LogServlet();
    }

    @Bean(name = "Manage")
    public ManageServlet Manage() {
        return new ManageServlet();
    }

    @Bean(name = "Move")
    public MoveServlet Move() {
        return new MoveServlet();
    }

    @Bean(name = "PasswordReset")
    public PasswordResetServlet PasswordReset() {
        return new PasswordResetServlet();
    }

    @Bean(name = "Printable")
    public PrintableServlet Printable() {
        return new PrintableServlet();
    }

    @Bean(name = "RecentChangesFeed")
    public RecentChangesFeedServlet RecentChangesFeed() {
        return new RecentChangesFeedServlet();
    }

    @Bean(name = "RecentChanges")
    public RecentChangesServlet RecentChanges() {
        return new RecentChangesServlet();
    }

    @Bean(name = "Register")
    public RegisterServlet Register() {
        return new RegisterServlet();
    }

    @Bean(name = "Roles")
    public RolesServlet Roles() {
        return new RolesServlet();
    }

    @Bean(name = "Search")
    public SearchServlet Search() {
        return new SearchServlet();
    }

    @Bean(name = "Setup")
    public SetupServlet Setup() {
        return new SetupServlet();
    }

    @Bean(name = "SpecialPages")
    public SpecialPagesServlet SpecialPages() {
        return new SpecialPagesServlet();
    }

    @Bean(name = "Stylesheet")
    public StylesheetServlet Stylesheet() {
        return new StylesheetServlet();
    }

    @Bean(name = "Topic")
    public TopicServlet Topic() {
        return new TopicServlet();
    }

    @Bean(name = "Translation")
    public TranslationServlet Translation() {
        return new TranslationServlet();
    }

    @Bean(name = "Upgrade")
    public UpgradeServlet Upgrade() {
        return new UpgradeServlet();
    }

    @Bean(name = "Upload")
    public UploadServlet Upload() {
        return new UploadServlet();
    }

    @Bean(name = "ViewSource")
    public ViewSourceServlet ViewSource() {
        return new ViewSourceServlet();
    }

    @Bean(name = "Watchlist")
    public WatchlistServlet Watchlist() {
        return new WatchlistServlet();
    }

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
