# Unavify App - Class Architecture & Responsibilities

## 🏗️ **Overall Architecture**
Your app follows **MVVM (Model-View-ViewModel)** pattern with **Repository Pattern** and **Dependency Injection** using **Hilt**.

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     UI Layer    │    │  ViewModel      │    │  Repository     │
│   (Activities)  │◄──►│   (Business     │◄──►│   (Data Access) │
│                 │    │    Logic)       │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Firebase      │
                       │  (Backend)      │
                       └─────────────────┘
```

---

## 📱 **UI Layer (Activities & Screens)**

### **1. SplashActivity.java**
**Purpose:** App entry point and navigation controller
**Responsibilities:**
- ✅ Check if user is logged in
- ✅ Check if user profile exists
- ✅ Navigate to appropriate screen:
  - `LoginScreenJava` (if not logged in)
  - `EditProfileActivity` (if logged in but no profile)
  - `HomeScreenJava` (if logged in with profile)

### **2. LoginScreenJava.java**
**Purpose:** User authentication screen
**Responsibilities:**
- ✅ Phone number input and validation
- ✅ OTP input and verification
- ✅ Integration with `AuthViewModelJava`
- ✅ Auto-fill OTP from SMS (via `SmsReceiverJava`)

### **3. HomeScreenJava.java**
**Purpose:** Main app screen after login
**Responsibilities:**
- ✅ Display user feed/posts
- ✅ Navigation to other screens
- ✅ Basic home functionality

### **4. ProfileActivity.java**
**Purpose:** User profile display and management
**Responsibilities:**
- ✅ Display user profile information
- ✅ Edit profile functionality
- ✅ Sign out functionality
- ✅ Integration with `ProfileViewModelJava`

### **5. EditProfileActivity.java**
**Purpose:** Profile creation and editing
**Responsibilities:**
- ✅ Username input and validation
- ✅ Profile image selection
- ✅ Profile data saving
- ✅ Integration with `ProfileViewModelJava`

### **6. AddPostActivity.java** ⭐ **NEWLY ENHANCED**
**Purpose:** Media posting (photos/videos)
**Responsibilities:**
- ✅ Camera and gallery integration
- ✅ Media validation (size, duration, resolution)
- ✅ Image compression and processing
- ✅ Video capture and selection
- ✅ Firebase Storage upload
- ✅ Firestore post creation
- ✅ Runtime permissions handling
- ✅ Progress tracking and error handling

---

## 🧠 **ViewModel Layer (Business Logic)**

### **1. AuthViewModelJava.java**
**Purpose:** Authentication business logic
**Responsibilities:**
- ✅ Phone number validation (Indian format)
- ✅ OTP sending and verification
- ✅ Authentication state management
- ✅ Error handling and user feedback
- ✅ Integration with `AuthRepositoryJava`

### **2. ProfileViewModelJava.java**
**Purpose:** Profile management business logic
**Responsibilities:**
- ✅ Profile existence checking
- ✅ Username uniqueness validation
- ✅ Profile data saving
- ✅ User profile loading
- ✅ Sign out functionality
- ✅ Integration with `UserProfileRepository`

---

## 📊 **Repository Layer (Data Access)**

### **1. AuthRepositoryJava.java**
**Purpose:** Firebase Authentication operations
**Responsibilities:**
- ✅ Firebase Auth integration
- ✅ Phone number authentication
- ✅ OTP sending and verification
- ✅ User session management
- ✅ Sign out functionality

### **2. UserProfileRepository.java**
**Purpose:** User profile data operations
**Responsibilities:**
- ✅ Firestore profile operations
- ✅ Username uniqueness checking
- ✅ Profile data CRUD operations
- ✅ Profile image handling

---

## 🔧 **Supporting Classes**

### **1. UnavifyApplication.java**
**Purpose:** Application entry point
**Responsibilities:**
- ✅ Hilt dependency injection setup
- ✅ Application lifecycle management

### **2. AppModule.java (DI)**
**Purpose:** Dependency injection configuration
**Responsibilities:**
- ✅ Firebase services injection
- ✅ Repository injection
- ✅ ViewModel injection

### **3. SmsReceiverJava.java**
**Purpose:** SMS interception for OTP auto-fill
**Responsibilities:**
- ✅ Listen for incoming SMS
- ✅ Extract OTP codes (6-digit)
- ✅ Auto-fill OTP in login screen
- ✅ Pattern matching for OTP detection

---

## 📦 **Data Models**

### **1. UserProfile.java**
**Purpose:** User profile data model
**Responsibilities:**
- ✅ Store user profile information
- ✅ Data validation
- ✅ Firestore serialization

### **2. Post.java (in AddPostActivity)**
**Purpose:** Post data model
**Responsibilities:**
- ✅ Store post information (media URL, caption, etc.)
- ✅ Firestore document structure
- ✅ Media type identification

### **3. State Classes**
- **AuthUiState:** Authentication UI state
- **UsernameCheckState:** Username validation state
- **ProfileSaveResult:** Profile save operation result
- **ValidationResult:** Media validation result

---

## 🔄 **Data Flow**

### **Authentication Flow:**
```
SplashActivity → LoginScreenJava → AuthViewModelJava → AuthRepositoryJava → Firebase Auth
```

### **Profile Flow:**
```
ProfileActivity → ProfileViewModelJava → UserProfileRepository → Firestore
```

### **Posting Flow:**
```
AddPostActivity → Media Processing → Firebase Storage → Firestore
```

---

## 🛡️ **Security & Permissions**

### **Runtime Permissions:**
- ✅ **Camera:** For taking photos/videos
- ✅ **Storage:** For accessing gallery
- ✅ **SMS:** For OTP auto-fill
- ✅ **Android 13+:** Granular media permissions

### **Security Features:**
- ✅ **FileProvider:** Secure file sharing
- ✅ **Input Validation:** Phone numbers, usernames
- ✅ **Media Validation:** File size, format, content
- ✅ **Firebase Security Rules:** Backend validation

---

## 🚀 **Key Features by Class**

| Class | Primary Feature | Secondary Features |
|-------|----------------|-------------------|
| `SplashActivity` | Navigation Logic | User State Check |
| `LoginScreenJava` | Phone Auth | OTP Auto-fill |
| `AuthViewModelJava` | Auth Business Logic | Validation |
| `AddPostActivity` | Media Posting | Compression, Validation |
| `ProfileViewModelJava` | Profile Management | Username Check |
| `SmsReceiverJava` | OTP Auto-fill | SMS Parsing |

---

## 📈 **Scalability Considerations**

### **Current Architecture Benefits:**
- ✅ **Separation of Concerns:** Clear layer separation
- ✅ **Dependency Injection:** Easy testing and maintenance
- ✅ **Repository Pattern:** Abstracted data access
- ✅ **MVVM:** Reactive UI updates
- ✅ **Background Processing:** Non-blocking operations

### **Future Enhancements:**
- 🔄 **Offline Support:** Local caching
- 🔄 **Real-time Updates:** Firestore listeners
- 🔄 **Push Notifications:** FCM integration
- 🔄 **Analytics:** User behavior tracking
- 🔄 **Content Moderation:** AI-powered filtering

---

## 🎯 **Summary**

Your Unavify app has a **well-structured, production-ready architecture** with:

- **Clean separation** between UI, business logic, and data layers
- **Robust authentication** with phone number verification
- **Comprehensive media posting** with validation and compression
- **Proper error handling** and user feedback
- **Security best practices** with runtime permissions
- **Scalable design** ready for future enhancements

The architecture follows Android best practices and is ready for production deployment! 🚀 