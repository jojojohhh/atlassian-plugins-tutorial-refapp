package com.atlassian.plugins.tutorial.refapp;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class MyPluginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(MyPluginServlet.class);

    @ComponentImport private final UserManager userManager;
    @ComponentImport private final LoginUriProvider loginUriProvider;

    @Autowired
    public MyPluginServlet(UserManager userManager, LoginUriProvider loginUriProvider) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
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
        res.setContentType("text/html");
        res.getWriter().write("<html><body>Hello! You did it.</body></html>");
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
