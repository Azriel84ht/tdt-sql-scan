package com.tdtsqlscan.web.dto;

import com.tdtsqlscan.web.domain.User;

public class RegistrationResult {

    private final User user;
    private final boolean resent;

    public RegistrationResult(User user, boolean resent) {
        this.user = user;
        this.resent = resent;
    }

    public User getUser() {
        return user;
    }

    public boolean isResent() {
        return resent;
    }
}
