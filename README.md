
# OrderUp Restaurant Simulator

OrderUp is a simple Java-based restaurant simulator game built with JavaFX. The application models a restaurant's customer flow, earnings, and other metrics over time. It is intended for educational and demo purposes and can be extended with additional features.

## 🧱 Project Structure
```
OrderUp/
├─ pom.xml               # Maven build file
├─ README.md             # Project documentation (this file)
├─ src/
│  └─ main/
│     ├─ java/com/example/game
│     │  ├─ Main.java          # Application entry point (JavaFX)
│     │  ├─ MenuManager.java   # Manages game menu/UI logic
│     │  ├─ Metrics.java       # Tracks and prints simulation metrics
│     │  └─ RestaurantSimulator.java # Core simulation logic
│     └─ resources/
│        └─ images/            # Game images and assets
└─ target/                  # Generated build artifacts (ignored)
```

## 🚀 Getting Started

### Prerequisites
- Java 17 (or later) with JavaFX support
- Maven 3.6+

### Build & Run
Use Maven to compile and launch the JavaFX application:
```powershell
mvn javafx:run
```
This will compile the code and open the simulator window.

## 🛠 Features
- **Simulation of customers over time** using `RestaurantSimulator`.
- **Metrics output** including a simple ASCII graph of customers vs. time (see `Metrics.getMetrics()`).
- Placeholder comments for additional metrics (spending vs earnings, rating vs time).

## 📈 Metrics Example
The `Metrics` class currently uses hard-coded arrays to demonstrate graphing:
```java
int[] time = {1, 2, 3, 4, 5};
int[] customers = {100, 120, 150, 130, 160};
```
Calling `Metrics.getMetrics()` prints a scaled bar chart to the console.

## 🎯 Extending the Project
You can expand OrderUp by:
1. Adding new metrics to `Metrics.java`.
2. Connecting `RestaurantSimulator` outputs to the UI.
3. Replacing hard-coded data with dynamic simulation input.
4. Enhancing the JavaFX interface in `Main.java` or `MenuManager.java`.

## 📄 License
This project is released under the MIT License. Feel free to modify and share.

---

*Setup instruction for Gemini API is no longer required by default, remove or keep if using the AI features.*