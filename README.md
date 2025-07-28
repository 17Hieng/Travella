# Travella - Malaysia Travel Planning App

Travella is an intelligent travel planning Android application specifically designed for Malaysia tourism. The app helps users plan perfect trips to Malaysia, providing attraction recommendations, route planning, budget management, and more.

## 🌟 Key Features

### 🔐 User Authentication
- **Google Sign-In Integration** - Secure Google account login using Firebase Authentication
- **Session Management** - Automatic login state preservation for seamless user experience

### 🗺️ Travel Planning
- **Smart Itinerary Planning** - Automatically generate travel plans based on user-selected locations, dates, and times
- **Malaysia-Wide Coverage** - Support for travel planning across all Malaysian states and federal territories
- **Flexible Time Settings** - Customizable start and end dates and times

### 🏛️ Attraction Recommendations
- **Popular Attractions Discovery** - Get local popular tourist attractions based on Google Places API
- **Smart Filtering** - Filter attractions based on location, ratings, and other criteria
- **Detailed Information Display** - Show attraction names, ratings, addresses, photos, and more

### 🛣️ Route Planning
- **Optimal Path Algorithm** - Use Dijkstra's algorithm to calculate shortest paths between attractions
- **Google Maps Integration** - Real-time route display and navigation information
- **Multi-Attraction Routes** - Support for intelligent route planning with multiple attractions

### 💰 Budget Management
- **Visual Budget Charts** - Create beautiful pie charts using AnyChart library to display budget allocation
- **Categorized Budget Tracking** - Manage budgets for transportation, food, entertainment, accommodation, and others
- **Real-time Budget Updates** - Dynamically update and display budget usage

### 🗺️ Map Features
- **Interactive Maps** - Complete map functionality based on Google Maps
- **Location Search** - Intelligent location search and autocomplete functionality
- **Marker Management** - Mark and manage tourist attractions on the map

## 🛠️ Technology Stack

### Frontend Technologies
- **Kotlin** - Primary development language
- **Android SDK** - Native Android development
- **Material Design** - Modern UI design
- **ConstraintLayout** - Responsive layout design

### Backend Services
- **Firebase Authentication** - User authentication service
- **Google Places API** - Location search and attraction information
- **Google Maps API** - Map and navigation services
- **Directions API** - Route planning service

### Third-party Libraries
- **OkHttp** - Network request handling
- **Retrofit** - REST API client
- **Gson** - JSON data parsing
- **Glide** - Image loading and caching
- **AnyChart** - Data visualization charts
- **Kotlinx Serialization** - JSON serialization

### Development Tools
- **Android Studio** - Primary development IDE
- **Gradle** - Build tool
- **Git** - Version control

## 📱 System Requirements

- **Android Version**: Android 8.0 (API 28) or higher
- **Device Requirements**: Device with Google Play Services support
- **Network Connection**: Internet connection required for map and API services
- **Storage Space**: Recommend at least 100MB available storage space

## 🚀 Installation Guide

### Development Environment Setup

1. **Clone the Project**
   ```bash
   git clone https://github.com/yourusername/Travella.git
   cd Travella
   ```

2. **Configure API Keys**
   - Create a `secrets.properties` file in the project root directory
   - Add the following configuration:
   ```properties
   MAPS_API_KEY=your_google_maps_api_key
   PLACES_API_KEY=your_google_places_api_key
   ```

3. **Configure Firebase**
   - Create a new project in Firebase Console
   - Download the `google-services.json` file and place it in the `app/` directory
   - Enable Google Sign-In authentication

4. **Build the Project**
   ```bash
   ./gradlew build
   ```

### Running the App

1. **Connect Android Device** or start an emulator
2. **Run the App**
   ```bash
   ./gradlew installDebug
   ```

## 📖 User Guide

### First Time Use
1. Launch the app and sign in with your Google account
2. Click the "Start Planning" button to enter the travel planning process

### Creating Travel Plans
1. **Select Destination** - Choose a travel destination from the Malaysian states list
2. **Set Time** - Select start and end dates and times
3. **Generate Plan** - The system will automatically generate recommended travel plans

### Exploring Attractions
1. **Browse Recommended Attractions** - View popular attractions recommended by the system
2. **View Details** - Click on attractions to see detailed information, ratings, addresses, etc.
3. **Add to Itinerary** - Select attractions of interest to add to your travel plan

### Route Planning
1. **Select Attractions** - Choose attractions to visit from the added locations
2. **Generate Route** - The system will use Dijkstra's algorithm to calculate the optimal route
3. **View Map** - View detailed routes and navigation information on the map

### Budget Management
1. **Set Budget** - Set budget amounts for different categories
2. **Track Expenses** - Update and track actual expenses in real-time
3. **View Charts** - Intuitively view budget allocation through pie charts

## 🏗️ Project Structure

```
Travella/
├── app/
│   ├── src/main/
│   │   ├── java/com/csian/travella/
│   │   │   ├── MainActivity.kt              # Main interface and login
│   │   │   ├── StartTravelPlanActivity.kt   # Travel planning entry
│   │   │   ├── TravelPlanFormActivity.kt    # Travel plan form
│   │   │   ├── MushTryActivity.kt           # Attraction recommendations
│   │   │   ├── Route.kt                     # Route planning
│   │   │   ├── TotalBudgetActivity.kt       # Budget management
│   │   │   └── ...                          # Other functional modules
│   │   ├── res/
│   │   │   ├── layout/                      # Layout files
│   │   │   ├── values/                      # Resource files
│   │   │   └── drawable/                    # Icons and images
│   │   └── AndroidManifest.xml              # App configuration
│   ├── build.gradle.kts                     # App-level build configuration
│   └── google-services.json                 # Firebase configuration
├── build.gradle.kts                         # Project-level build configuration
└── README.md                                # Project documentation
```

## 🔧 Configuration Guide

### API Key Configuration
The app requires the following API keys to function properly:

1. **Google Maps API Key** - For map display and route planning
2. **Google Places API Key** - For location search and attraction information
3. **Firebase Configuration** - For user authentication

### Permission Requirements
The app requires the following permissions:
- `ACCESS_COARSE_LOCATION` - Location information access
- `INTERNET` - Network access
- `ACCESS_NETWORK_STATE` - Network status checking

## 🤝 Contributing

We welcome code contributions! Please follow these steps:

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## 📞 Contact

- Project Maintainer: [Your Name]
- Email: [your.email@example.com]
- Project Link: [https://github.com/yourusername/Travella](https://github.com/yourusername/Travella)

## 🙏 Acknowledgments

- Google Maps Platform for map and location services
- Firebase for authentication services
- AnyChart for data visualization library
- All developers and users who have contributed to the project

---

**Travella** - Make your Malaysia journey more exciting! 🇲🇾✈️
