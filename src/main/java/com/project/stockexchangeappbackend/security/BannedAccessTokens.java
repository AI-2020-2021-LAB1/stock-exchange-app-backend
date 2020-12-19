package com.project.stockexchangeappbackend.security;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.TreeMap;

public class BannedAccessTokens {

    private final Map<String, OffsetDateTime> users;

    public BannedAccessTokens() {
        users = new TreeMap<>();
    }

    public boolean isBanned(String username, OffsetDateTime tokenIssuedAt) {
        return users.containsKey(username) && tokenIssuedAt.isBefore(users.get(username));
    }

    public void addUser(String username) {
        users.put(username, OffsetDateTime.now(ZoneId.systemDefault()));
    }

}
