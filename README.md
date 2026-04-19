# Client Portal Pro (by TrackIQ)

![Android CI Build](https://github.com/YOUR_USERNAME/trackiq-client-portal/actions/workflows/android.yml/badge.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Platform](https://img.shields.io/badge/Platform-Android-green.svg)

A premium, native Android client portal designed specifically for digital agencies and freelancers. This micro-SaaS platform provides a secure, white-labeled hub for clients to track active project phases, view project progress, and manage billing securely.

## 🚀 Features
* **Secure Authentication:** Powered by Firebase Auth (Email/Password).
* **Offline-First Architecture:** Utilizes Cloud Firestore for local data caching, ensuring clients can view their dashboard even with a poor network connection.
* **Real-Time Dashboard:** Live progress tracking and project phase updates.
* **CI/CD Pipeline:** Automated APK builds on every push to the `main` branch via GitHub Actions.
* **Enterprise UI/UX:** Built natively with Java and Material Design Components.

## 🛠 Tech Stack
* **Language:** Java (JDK 17)
* **Minimum SDK:** API 24 (Android 7.0)
* **Target SDK:** API 34 (Android 14)
* **Architecture:** MVC / ViewBinding
* **Backend & Database:** Firebase BoM (Auth, Firestore)
* **CI/CD:** GitHub Actions

## ⚙️ Getting Started

### Prerequisites
1. [Android Studio](https://developer.android.com/studio) (Latest version recommended).
2. A Firebase Account.

### Installation
1. Clone the repository:
   ```bash
   git clone [https://github.com/YOUR_USERNAME/trackiq-client-portal.git](https://github.com/YOUR_USERNAME/trackiq-client-portal.git)
