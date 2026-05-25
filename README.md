# Beyond The Galaxy 🚀

A JavaFX-based space-themed RPG game featuring multiple game modes, multiplayer PvP battles, character progression, and an in-game marketplace.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [How to Run](#how-to-run)
- [Game Mechanics](#game-mechanics)
- [Project Structure](#project-structure)
- [User Data](#user-data)
- [Contributing](#contributing)

## 🎮 Overview

**Beyond The Galaxy** is an immersive space adventure game where players navigate through various levels, battle monsters, collect resources, upgrade their characters, and compete against other players in real-time PvP combat. The game features a comprehensive progression system with weapons, shields, special cards, and a player-driven marketplace.

## 🎬 Gameplay Demo
**[▶️ Watch Full Gameplay Demo on YouTube](https://youtu.be/hDRxOb0d7O4)**

Click the link above to watch the complete gameplay demonstration showcasing all features in action!

## 📸 Screenshots

<p align="center">
  <img src="screenshots/Screenshot 2026-02-10 212538.png" width="45%" />
  <img src="screenshots/Screenshot 2026-02-10 212552.png" width="45%" />
</p>
<p align="center">
  <img src="screenshots/Screenshot 2026-02-10 212607.png" width="45%" />
  <img src="screenshots/Screenshot 2026-02-10 212636.png" width="45%" />
</p>
<p align="center">
  <img src="screenshots/Screenshot 2026-02-10 212849.png" width="45%" />
  <img src="screenshots/Screenshot 2026-02-10 212903.png" width="45%" />
</p>
<p align="center">
  <img src="screenshots/Screenshot 2026-02-10 212931.png" width="45%" />
  <img src="screenshots/Screenshot 2026-02-10 213124.png" width="45%" />
</p>

## ✨ Features

### Game Modes
- **Guest Mode**: Play without creating an account - explore the game world freely
- **Story Mode**: Progress through 3 main levels (Level 1, 2, 3) with increasing difficulty
- **Phase Mode**: Additional 3 phases with unique challenges
- **PvP Mode**: Real-time multiplayer battles against other players

### Core Systems
- **Account System**: Create and manage player accounts with secure login
- **Character Progression**:
  - Collect and upgrade weapons
  - Enhance shields for better defense
  - Acquire special cards with unique powers (6 different cards)
  - Track total character power
- **Resource Management**: Collect 3 types of resources to build your spaceship
- **Gem Economy**: Earn and spend gems throughout the game
- **Shop Marketplace**:
  - Buy special cards from other players
  - Sell your own cards at custom prices
  - Real-time marketplace updates
- **Global Chat**: Communicate with other online players
- **Leaderboard**: Track top players and rankings
- **Sound System**: Background music and sound effects with mute controls

### Combat Features
- **Projectile-based Combat**: Shoot projectiles in all directions (standard 4 directions, with 8-directional and 16-directional spell patterns for card upgrades)
- **Active Card Combat System**: Cast powerful combat spells by pressing **`E`** based on your highest owned Special Card (includes glassmorphism card slot, cooldown overlay, circular particle bursts, pulsing shields, chronos time warp time-dilation, vampiric heal, and giga laser!)
- **Instant-Action Enemy Relocation**: Relocated initial monsters along the main path to provide instant gameplay engagement right at the starting area, and extended their player detection triggers to cover your initial spawn coordinates.
- **Monster AI**: Intelligent enemies that track and attack the player
- **Health & Oxygen Systems**: Manage your character's vitals
- **Monster Respawning**: Dynamic enemy respawn mechanics
- **Explosion Effects**: Visual feedback for combat

## 🛠️ Technology Stack

- **Language**: Java 23 / 25
- **UI Framework**: JavaFX 17.0.6
- **Game Engine**: FXGL 17.3
- **Build Tool**: Maven
- **Additional Libraries**:
  - ControlsFX 11.1.2
  - FormsFX 11.6.0
  - BootstrapFX 0.4.0
- **Testing**: JUnit 5.10.0

## 📦 Prerequisites

- **Java Development Kit (JDK)**: Version 23 / 25 or higher
- **Maven**: Version 3.6 or higher
- **JavaFX**: Included via Maven dependencies

## 🔧 Installation

1. **Clone the repository** (or download the project):
   ```bash
   git clone <repository-url>
   cd FinalBTG
   ```

2. **Verify Java installation**:
   ```bash
   java -version
   ```
   Ensure you have JDK 23 or higher installed.

3. **Verify Maven installation**:
   ```bash
   mvn -version
   ```

4. **Install dependencies**:
   ```bash
   mvn clean install
   ```

## 🚀 How to Run

### Using Maven

Run the game using the JavaFX Maven plugin:

```bash
mvn clean javafx:run
```

### Using IDE (IntelliJ IDEA / Eclipse)

1. Import the project as a Maven project
2. Wait for Maven to download all dependencies
3. Locate the main class: `com.example.finalbtg.MainGame`
4. Run the `MainGame` class

### Building Executable

To create a distributable package:

```bash
mvn clean package
```

## 🎯 Game Mechanics

### Getting Started

1. **Launch the Game**: Start from the home page
2. **Choose Mode**:
   - **Guest**: Play immediately without account
   - **Login**: Access full features with an account

### Account Creation

- Click "Sign up" on the login page
- Enter a unique username and password
- New accounts start with:
  - 0 gems
  - 0 resources
  - Basic weapon and shield
  - No special cards

### Gameplay

#### Controls
- **Arrow Keys / WASD**: Move character
- **Mouse Click**: Shoot projectiles in the direction of the cursor
- **F Key**: Shoot projectile in current facing direction
- **E Key**: Cast equipped Special Card active combat spell
- **SPACE**: Activate speed boost (when picked up)

#### Levels & Phases
- **Level 1-3**: Story progression with increasing difficulty
- **Phase 1-3**: Additional challenges with unique maps
- Each level has:
  - Custom map layout (defined in `level1map.txt`, `level2map.txt`, etc.)
  - Different monster types and quantities
  - Resource collection opportunities
  - Gate to next level (unlocks when all monsters are defeated)

#### Combat System
- Shoot projectiles to damage monsters
- Monsters shoot back and chase the player
- Collect gems from defeated monsters
- Manage health and oxygen levels
- Avoid obstacles and monster projectiles

#### Character Progression
1. **Collect Gems**: Defeat monsters and complete levels
2. **Gather Resources**: Find resources to upgrade spaceship (10 of each type needed)
3. **Acquire Special Cards**: Purchase from shop or earn through gameplay
4. **Upgrade Equipment**: Improve weapons and shields
5. **Increase Power**: Total power = Weapon + Shield + Card Power

#### Shop System
- **Buy Cards**: Browse cards listed by other players
- **Sell Cards**: Set your own price and list cards for sale
- **Real-time Updates**: See new listings as they appear
- Requires active server connection

#### PvP Mode
- **Matchmaking**: Wait for an opponent to connect
- **Real-time Combat**: Battle against another player
- **Custom Arena**: Dedicated PvP map
- Uses your character's stats (weapon, shield, card power)

#### Chat System
- **Global Chat**: Talk with all online players
- **Auto-connect**: Connects when you open chat
- **Persistent**: Chat history maintained during session

### User Profile
- View your username
- Check total gems
- See collected resources (x/10 for each type)
- Review character stats:
  - Weapon power
  - Shield power
  - Special card power
  - Total character power
- View card collection

## 📁 Project Structure

```
FinalBTG/
├── src/
│   └── main/
│       ├── java/com/example/finalbtg/
│       │   ├── MainGame.java              # Application entry point
│       │   ├── ButtonHandler.java         # UI controller for all menus
│       │   ├── MainCharacter.java         # Player character class
│       │   ├── AllCharacter.java          # Base character class
│       │   ├── Monster.java               # Monster base class
│       │   ├── loginMonster.java          # Monster for story levels
│       │   ├── Pet.java                   # Pet companion class
│       │   ├── loginPet.java              # Pet for story levels
│       │   ├── Projectile.java            # Projectile mechanics
│       │   ├── CombatUpgradeHelper.java   # Active Special Card HUD slot and custom combat skills
│       │   ├── PvpProjectile.java         # PvP projectile mechanics
│       │   ├── Resource.java              # Resource collection
│       │   ├── Level1Controller.java      # Level 1 UI controller
│       │   ├── Level1Loop.java            # Level 1 game loop
│       │   ├── Level2Controller.java      # Level 2 UI controller
│       │   ├── Level2Loop.java            # Level 2 game loop
│       │   ├── Level3Controller.java      # Level 3 UI controller
│       │   ├── Level3Loop.java            # Level 3 game loop
│       │   ├── Phase1Controller.java      # Phase 1 UI controller
│       │   ├── Phase1Loop.java            # Phase 1 game loop
│       │   ├── Phase2Controller.java      # Phase 2 UI controller
│       │   ├── Phase2Loop.java            # Phase 2 game loop
│       │   ├── Phase3Controller.java      # Phase 3 UI controller
│       │   ├── Phase3Loop.java            # Phase 3 game loop
│       │   ├── GuestGameController.java   # Guest mode controller
│       │   ├── GuestGameLoop.java         # Guest mode game loop
│       │   ├── PvpController.java         # PvP UI controller
│       │   ├── PvpLoop.java               # PvP game loop
│       │   ├── PvpClient.java             # PvP client networking
│       │   ├── PvpClientHandler.java      # PvP client handler
│       │   ├── PvpServer.java             # PvP server
│       │   ├── ChatClient.java            # Chat client
│       │   ├── ChatServer.java            # Chat server
│       │   ├── ShopClient.java            # Shop client
│       │   └── ShopServer.java            # Shop server
│       └── resources/
│           ├── com/example/finalbtg/
│           │   ├── homePage.fxml          # Home screen
│           │   ├── loginPage.fxml         # Login/signup screen
│           │   ├── loginDashboard.fxml    # Main dashboard
│           │   ├── guestDashboard.fxml    # Guest dashboard
│           │   ├── guestGameWorld.fxml    # Guest game world
│           │   ├── levels.fxml            # Level selection
│           │   ├── level1.fxml            # Level 1 UI
│           │   ├── level2.fxml            # Level 2 UI
│           │   ├── level3.fxml            # Level 3 UI
│           │   ├── phase1.fxml            # Phase 1 UI
│           │   ├── phase2.fxml            # Phase 2 UI
│           │   ├── phase3.fxml            # Phase 3 UI
│           │   └── pvp.fxml               # PvP UI
│           ├── image/                     # Game images and sprites
│           └── bgm.mp3                    # Background music
├── user/                                  # User account data (created at runtime)
├── level1map.txt                          # Level 1 map data
├── level2map.txt                          # Level 2 map data
├── level3map.txt                          # Level 3 map data
├── phase1map.txt                          # Phase 1 map data
├── phase2map.txt                          # Phase 2 map data
├── phase3map.txt                          # Phase 3 map data
├── pvpmap.txt                             # PvP map data
├── worldmap.txt                           # World map data
├── onSale.txt                             # Shop marketplace data
├── pom.xml                                # Maven configuration
└── README.md                              # This file
```

## 💾 User Data

User accounts are stored in the `user/` directory as text files:

**Format**: `username.txt`

**Contents**:
```
username,password
resource,amount1,amount2,amount3
gems,totalGems
weapon,weaponPower
shield,shieldPower
cardPower,cardPowerValue
specialcard,cardId1,cardId2,...
```

**Example**:
```
player1,password123
resource,5,7,3
gems,1500
weapon,250
shield,300
cardPower,1200
specialcard,1,3,5
```

## 🤝 Contributing

This is an educational project. If you'd like to contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is created for educational purposes.

## 🎮 Gameplay Tips

1. **Start with Guest Mode** to learn the controls
2. **Create an Account** to save progress and access all features
3. **Collect Resources** early - you need 10 of each type
4. **Save Gems** for purchasing powerful special cards in the shop
5. **Upgrade Strategically** - balance weapon, shield, and card power
6. **Use Chat** to coordinate with other players or get tips
7. **Practice in Story Mode** before trying PvP
8. **Check the Leaderboard** to see how you rank against others

## 🐛 Troubleshooting

### Game won't start
- Verify JDK 23 is installed: `java -version`
- Ensure Maven dependencies are installed: `mvn clean install`

### Black screen or missing graphics
- Check that the `resources/image/` directory contains all required images
- Verify JavaFX is properly configured

### Multiplayer features not working
- Ensure the server components start automatically
- Check firewall settings if connecting across networks
- Multiple players need to be online for PvP and chat

### Audio not playing
- Verify `bgm.mp3` exists in `resources/`
- Check system audio settings
- Use the in-game mute toggle

---

**Enjoy your journey Beyond The Galaxy! 🌌**
