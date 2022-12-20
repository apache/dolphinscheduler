package org.apache.dolphinscheduler.api.security;

import org.apache.dolphinscheduler.api.controller.LoginController;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

public final class LoginCsrfTokenRepository implements CsrfTokenRepository {

    public static final String PARAMETER_NAME = "_csrf";
    public static final String HEADER_NAME = "X-XSRF-TOKEN";
    public static final String COOKIE_NAME = "csrfToken";

    public LoginCsrfTokenRepository() {
    }

    /**
     * The csrf token will be generated in {@link CsrfFilter}
     * @param request http servlet request
     */
    public CsrfToken generateToken(HttpServletRequest request) {
        return new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, createNewToken());
    }

    /**
     * The csrf token will be stored in the http request so that the login api {@link LoginController} can obtain the csrf token
     * @param token csrf token generated in {@link CsrfFilter}
     * @param request http servlet request
     * @param response http servlet response
     */
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {

    }

    /**
     * load csrf token from http request cookie
     * @param request http servlet request
     * @return csrf token
     */
    public CsrfToken loadToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
        if (cookie == null) {
            return null;
        } else {
            String token = cookie.getValue();
            return !StringUtils.hasLength(token) ? null : new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, token);
        }
    }

    private String createNewToken() {
        return UUID.randomUUID().toString();
    }

    private String getRequestContext(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath.length() > 0 ? contextPath : "/";
    }

}
