package com.unavify.app.ui.profile;

public abstract class ProfileSaveResult {
    public static class Success extends ProfileSaveResult {}
    public static class Error extends ProfileSaveResult {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
    public static class Loading extends ProfileSaveResult {}
} 