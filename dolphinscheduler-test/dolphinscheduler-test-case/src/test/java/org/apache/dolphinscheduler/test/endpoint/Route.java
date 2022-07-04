package org.apache.dolphinscheduler.test.endpoint;

public class Route {

    public static String login() {
        return "/login";
    }

    public static String tenants() {
        return "/tenants";
    }

    public static String tenants(int id) {
        return "/tenants/" + String.valueOf(id);
    }

    public static String tenantsList() {
        return "/tenants/list";
    }

    public static String tenantsVerifyCode() {
        return "/tenants/verify-code";
    }

    public static String accessTokens() {
        return "/access-tokens";
    }

    public static String accessTokens(int id) {
        return "/access-tokens";
    }

    public static String accessTokenByUser(int userId) {
        return accessTokens() + "/user/" + String.valueOf(userId);
    }

    public static String generateAccessToken() {
        return accessTokens() + "/generate";
    }


}
