package com.printway.config;

import java.io.Serializable;

public class Database implements Serializable {
    private final String url = "jdbc:mysql://192.168.1.188/orderdesk";
    private final String username = "theanh";
    private final String password = "pW@@theanh123";

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
