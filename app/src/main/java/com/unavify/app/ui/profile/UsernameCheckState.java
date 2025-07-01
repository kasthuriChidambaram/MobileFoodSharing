package com.unavify.app.ui.profile;

public abstract class UsernameCheckState {
    public static class Loading extends UsernameCheckState {}
    public static class Available extends UsernameCheckState {}
    public static class Taken extends UsernameCheckState {
        private final String message;
        public Taken(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
    public static class Error extends UsernameCheckState {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
} 