package com.aluminate.aluminate_organization_backend.dto.login;

import com.aluminate.aluminate_organization_backend.model.Member;

public class LoginResponse {
    private String token;
    private Member member;

    public LoginResponse(String token, Member member) {
        this.token = token;
        this.member = member;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
