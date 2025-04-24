NoteNest 📝
Overview
NoteNest is a modern note-taking Android application that allows users to create, edit, and delete notes seamlessly, with real-time synchronization using Firebase. It also features a motivational quote system, fetching daily quotes from a custom Express.js API deployed on Railway. The app supports offline functionality, ensuring a smooth user experience even without an internet connection.
Features

User Authentication: Sign up and log in securely using Firebase Authentication.
Note Management:
Add, edit, and delete notes with a clean, intuitive UI.
Swipe-to-delete notes with an undo option via Snackbar.
Offline support: Notes are cached locally using Room and synced with Firebase when online.


Motivational Quotes:
Displays random motivational quotes fetched from a custom API.
Offline fallback quote if the API is unreachable.


Real-Time Sync: Notes are synced in real-time with Firebase Firestore.
Sign-Out Protection: Prevents sign-out when offline to ensure session security.
Responsive UI: Edge-to-edge display with a modern design, including a Floating Action Button (FAB) for adding notes.

Tech Stack
Android App

Language: Kotlin
Architecture: MVVM (Model-View-ViewModel)
Dependencies:
Firebase (Authentication, Firestore)
Room (Local database for offline support)
Retrofit (API calls for quotes)
Hilt (Dependency Injection)
Coroutines (Asynchronous programming)
View Binding
RecyclerView (For note list)
Material Design Components (Snackbar, FAB, etc.)


Minimum SDK: 21 (Android 5.0 Lollipop)

Backend API

Framework: Express.js (Node.js)
Deployment: Railway
Endpoint: /quotes (Returns a list of motivational quotes)
Port: Configurable via environment variable (PORT)

Prerequisites

Android Studio: Latest stable version (e.g., Android Studio Iguana or later)
Firebase Account: For Authentication and Firestore setup
Node.js: For running the backend API locally (v16 or later recommended)
Git: For version control and publishing to GitHub

Setup Instructions
1. Clone the Repository
git clone https://github.com/<your-username>/notenest.git
cd notenest

2. Android App Setup

Open the Project:
Open the app directory in Android Studio.


Firebase Configuration:
Create a Firebase project in the Firebase Console.
Add an Android app to your Firebase project (use package name com.example.notenest).
Download the google-services.json file and place it in app/src/main.
Enable Email/Password Authentication in the Firebase Authentication section.
Enable Firestore in the Firebase Console and create a collection named notes.


Sync Project:
Sync the project with Gradle in Android Studio (File > Sync Project with Gradle Files).


Run the App:
Connect an emulator or physical device.
Run the app (Run > Run 'app').



3. Backend API Setup

Navigate to the API Directory:
The backend API code is in the notenest-api directory.

cd notenest-api


Install Dependencies:npm install


Run Locally:npm start


The API will run on http://localhost:3000 (or the port specified in PORT environment variable).


Deploy to Railway:
Create a Railway account at railway.app.
Link your GitHub repository and deploy the notenest-api directory.
Generate a public domain (e.g., notenest-api-production.up.railway.app).
Update the baseUrl in app/src/main/java/com/example/notenest/network/NoteModule.kt to the Railway domain.



4. Offline Support

The app uses Room to cache notes locally.
Notes added offline will sync with Firebase Firestore when the internet is restored.
Quotes will fall back to a hardcoded quote if the API is unreachable.

Usage

Sign Up/Login:
Launch the app and sign up with an email and password, or log in if you already have an account.


Manage Notes:
Use the FAB to add a new note.
Swipe left or right on a note to delete it (with an undo option).
Tap a note to edit it in the detail screen.


View Quotes:
A random motivational quote is displayed at the top of the main screen.
Use the retry button to fetch a new quote.


Sign Out:
Click the logout button to sign out (only available when online).



Project Structure

app/: Android app source code
data/: Data models, Room database, and DAO
network/: Retrofit setup for API calls
repository/: Repository for data operations
vm/: ViewModel for MVVM architecture
ui/: Activities and adapters


notenest-api/: Backend API source code
index.js: Express.js server with /quotes endpoint


Contributing
Contributions are welcome! Please follow these steps:

Fork the repository.
Create a new branch (git checkout -b feature/your-feature).
Commit your changes (git commit -m "Add your feature").
Push to the branch (git push origin feature/your-feature).
Open a Pull Request.

License
This project is licensed under the MIT License - see the LICENSE file for details.
Contact
For questions or feedback, reach out to:

GitHub: MuhammadZeeshan00
Email: your. muhammadzeeshan1806@gmail.com

