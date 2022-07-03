package org.apache.dolphinscheduler.test.apis;

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




}
