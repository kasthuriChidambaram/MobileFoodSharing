# Unavify - Food Recipe Community App

Unavify is an Instagram-like app focused on sharing food recipes. The app features phone-based authentication and is designed to build a community around home-cooked food content.

## Features

### Authentication
- **Phone Number Login**: Users can sign in using only their phone number
- **OTP Verification**: One-time password verification via SMS
- **Automatic OTP Reading**: Android devices can automatically read OTP from SMS messages
- **No Email/Password**: Simplified authentication flow for faster onboarding

### App Structure
- **Clean Architecture**: MVVM pattern with Repository pattern
- **Jetpack Compose**: Modern UI built with Compose
- **Firebase Integration**: Authentication and backend services
- **Hilt Dependency Injection**: Clean dependency management
- **Material Design 3**: Modern and beautiful UI

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository Pattern
- **Dependency Injection**: Hilt
- **Authentication**: Firebase Authentication
- **Backend**: Firebase (Firestore, Storage, etc.)
- **Navigation**: Compose Navigation
- **State Management**: StateFlow and Compose State

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 24+
- Firebase project with Authentication enabled

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Unavify
   ```

2. **Firebase Setup**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Phone Authentication in Firebase Console
   - Download `google-services.json` and place it in the `app/` directory
   - The current configuration uses the provided Firebase credentials

3. **Build and Run**
   ```bash
   ./gradlew build
   ```
   
   Or open the project in Android Studio and run it directly.

### Firebase Configuration

The app is configured with the following Firebase settings:
- **Project ID**: unavify
- **API Key**: AIzaSyAA6C0bEgFB7YWOJ9rLyhQmTisQTCFjOGI
- **Auth Domain**: unavify.firebaseapp.com
- **Storage Bucket**: unavify.appspot.com

## App Flow

1. **Launch**: App checks authentication state
2. **Login Screen**: User enters phone number
3. **OTP Verification**: User receives and enters 6-digit OTP
4. **Auto-verification**: App can automatically read OTP from SMS
5. **Home Screen**: User is taken to the main app interface
6. **Sign Out**: User can sign out and return to login

## Permissions

The app requires the following permissions:
- `INTERNET`: For Firebase communication
- `RECEIVE_SMS`: For automatic OTP reading
- `READ_SMS`: For automatic OTP reading

## Future Features

### Planned Implementation
- **Recipe Posting**: Share food recipes with photos and descriptions
- **Monthly Contests**: Health-based recipe competitions
- **Community Features**: Like, comment, and follow other users
- **Rewards System**: Points and rewards for participation
- **Recipe Discovery**: Browse and search recipes
- **User Profiles**: Personal recipe collections and achievements

### Revenue Model
- **Contest Sponsorships**: Partner with food brands for contests
- **Premium Features**: Advanced recipe tools and analytics
- **Advertising**: Sponsored content and recipe recommendations
- **Affiliate Marketing**: Product recommendations and links

## Architecture

```
app/
├── data/
│   └── repository/
│       └── AuthRepository.kt
├── di/
│   └── AppModule.kt
├── receiver/
│   └── SmsReceiver.kt
├── ui/
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   └── LoginScreen.kt
│   ├── home/
│   │   └── HomeScreen.kt
│   └── theme/
└── MainActivity.kt
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please contact the development team or create an issue in the repository. 