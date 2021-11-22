package com.atlassian.plugins.tutorial.refapp;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@ExportAsDevService
@Component
public class MyPluginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(MyPluginServlet.class);
    private static final String PLUGIN_STORAGE_KEY = "com.atlassian.plugins.tutorial.refapp.adminui";

    private final UserManager userManager;
    private final LoginUriProvider loginUriProvider;
    private final TemplateRenderer templateRenderer;
    private final PluginSettingsFactory pluginSettingsFactory;

    @Autowired
    public MyPluginServlet(@ComponentImport UserManager userManager,@ComponentImport LoginUriProvider loginUriProvider, @ComponentImport TemplateRenderer templateRenderer, @ComponentImport PluginSettingsFactory pluginSettingsFactory) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.templateRenderer = templateRenderer;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        UserProfile user = userManager.getRemoteUser(req);
        boolean isUserAdmin = userManager.isSystemAdmin(user.getUserKey());
        String username = user.getUsername();

        if (username == null || !isUserAdmin) {
            redirectToLogin(req, res);
            return;
        }

        Map<String, Object> context = new HashMap<>();

        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        if (settings.get(PLUGIN_STORAGE_KEY + ".name") == null) {
            String noName = "Enter a name here.";
            settings.put(PLUGIN_STORAGE_KEY + ".name", noName);
        }

        if (settings.get(PLUGIN_STORAGE_KEY + ".age") == null) {
            String noAge = "Enter a age here.";
            settings.put(PLUGIN_STORAGE_KEY + ".age", noAge);
        }

        context.put("name", settings.get(PLUGIN_STORAGE_KEY + ".name"));
        context.put("age", settings.get(PLUGIN_STORAGE_KEY + ".age"));
        res.setContentType("text/html;charset=utf-8");
        templateRenderer.render("admin.vm", res.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        settings.put(PLUGIN_STORAGE_KEY + ".name", req.getParameter("name"));
        settings.put(PLUGIN_STORAGE_KEY + ".age", req.getParameter("age"));
        res.sendRedirect("test");
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.sendRedirect(loginUriProvider.getLoginUri(getUri(req)).toASCIIString());
    }

    private URI getUri(HttpServletRequest req) {
        StringBuffer sb = req.getRequestURL();
        if (req.getQueryString() != null) {
            sb.append("?");
            sb.append(req.getQueryString());
        }
        return URI.create(sb.toString());
    }
}
