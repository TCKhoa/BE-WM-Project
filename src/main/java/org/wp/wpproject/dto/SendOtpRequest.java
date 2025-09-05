package org.wp.wpproject.dto;

public class SendOtpRequest {
    private String email;

    public SendOtpRequest() {
    }

    public SendOtpRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "SendOtpRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}
