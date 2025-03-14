# Uber Clone (Demo)

This project is a demo implementation of an Uber-like Android application built in Kotlin using Android Studio. It showcases key features such as:

- **Two Always-Expanded SearchViews:**  
  One for Pickup and one for Drop locations.
- **Location Suggestions:**  
  As the user types (starting from three characters), matching locations are fetched using the Geocoder API and displayed in a suggestion list.
- **Google Maps Integration:**  
  The app displays a map using Google Maps API, marks the searched locations, and animates the camera to the selected point.
- **Pre-Filled Pickup Location:**  
  The Pickup SearchView is automatically pre-filled with the user's current address using reverse geocoding.
- **Bottom Sheet Ride Details:**  
  When a drop location is selected, a professional-looking bottom sheet (using Material Components) displays ride details (pickup, drop, estimated fare) and lets the user confirm or cancel the booking.

> **Note:** This demo uses static dummy values for fare calculation. In a production app, fare and ride details would be calculated using real data and backend APIs.

## Features

- **Google Maps Integration:**  
  Displays the map and user location.

- **Location Suggestions:**  
  Fetches and displays location suggestions in a RecyclerView.

- **Reverse Geocoding:**  
  Automatically pre-fills the pickup location based on the user's current location.

- **Professional UI:**  
  A polished bottom sheet for confirming ride details before booking.

## Prerequisites

- **Android Studio** (recommended version: Electric Eel or later)
- **Kotlin 1.5+**
- **Google Maps API Key:**  
  You need to obtain an API key from the [Google Cloud Console](https://console.cloud.google.com/) and add it to your AndroidManifest.xml.
- **Dependencies:**
    - Google Play Services (Maps & Location)
    - Material Components for Android
    - Kotlin Coroutines

## Download APK

Click below to download the latest version of the app:

[![Download APK](https://img.shields.io/badge/Download-APK-blue?style=for-the-badge&logo=android)](APK/app-debug.apk)

## Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/your-username/uber-clone-demo.git
