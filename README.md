# Merciless Warrior: Java Swing 2D Platformer Game

![alt text](https://img.shields.io/badge/Java-17-orange.svg?style=for-the-badge&logo=java)
![alt text](https://img.shields.io/badge/Kotlin-1.9-blue.svg?style=for-the-badge&logo=kotlin)
![alt text](https://img.shields.io/badge/2D%20Engine-Swing-purple.svg?style=for-the-badge)
![alt text](https://img.shields.io/badge/Spring-Boot-green.svg?style=for-the-badge&logo=spring)
![alt text](https://img.shields.io/badge/Docker-Compose-blue.svg?style=for-the-badge&logo=docker)
![alt text](https://img.shields.io/badge/PostgreSQL-darkblue.svg?style=for-the-badge&logo=postgresql)
![alt text](https://img.shields.io/badge/Redis-red.svg?style=for-the-badge&logo=redis)

<p float="left">
<img src="images/example.gif" alt="Gif1" width="60%">
</p>

## Table of Contents

- [About The Project](#about-the-project)
- [Key Features](#key-features)
  - [Gameplay Features](#gameplay-features)
  - [Technical Features](#technical-features)
- [Architecture Overview](#architecture-overview)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation & Launch](#installation--launch)
- [Acknowledgements](#acknowledgements)
- [License](#license)

## About The Project

As a developer, my goal was to create a complete, end-to-end gaming experience that combines the nostalgia of 2D platformers with the power of modern backend technologies. "Merciless Warrior" was born out of a passion for both game development and software engineering.

The game client is intentionally built using **Java Swing**, to explore the challenges of creating a game engine from scratch.

The backend is a sophisticated **microservices system** designed for scalability and separation of concerns. It handles everything from player accounts and cloud saves to leaderboards, demonstrating a full-stack development approach.

This project is a personal journey into the depths of game creation and backend architecture.

## Key Features

### Gameplay Features

- **Engaging 2D Platformer Gameplay:** Navigate through diverse levels, battling a variety of enemies with unique attack patterns.
- **Rich Combat System:** Combine melee attacks, special abilities, and powerful spells to overcome foes.
- **Deep Itemization:** Buy, sell, craft, and collect a wide array of items.
- **Character Progression:** Upgrade your character by collecting coins and tokens, unlocking new perks and enhancing your abilities.
- **Open-World Structure:** Explore a connected world, with the ability to backtrack to previous levels.
- **Challenging Boss Fights:** Test your skills against formidable bosses.
- **Interactive World:** Talk to various NPCs, embark on quests, and destroy objects.
- **Dynamic Soundscapes:** Experience a rich soundscape with music and sound powered by the **OpenAL** library for high-quality audio processing.
- **Save & Load System:** Save your progress locally on your computer or in the cloud to continue your adventure from anywhere.
- **Competitive Leaderboard:** Compete with other players and climb the ranks on the global leaderboard.

### Technical Features

- **Custom Game Engine:** Built entirely from scratch using Java Swing.
- **Microservices Architecture:** The backend is composed of four distinct services:
  - **Authentication Service:** Manages user registration, login, and secure JWT-based authentication.
  - **Game Service:** Handles all core game data, including player progression, inventory, and leaderboards.
  - **API Gateway:** A single entry point for all client requests, routing them to the appropriate service.
  - **Service Registry:** Allows services to dynamically discover and communicate with each other.
- **Containerized & Orchestrated:** The backend is containerized with **Docker** and managed with **Docker Compose**.
- **Resilient by Design:** The Game Service uses resilience patterns **Circuit Breakers** and **Retries** to handle potential failures in inter-service communication.
- **Secure by Default:** The backend uses **Spring Security** for authentication and authorization.

## Architecture Overview

```mermaid
graph TD
    subgraph User
        Player["👤<br><b>Player</b>"]
    end

    subgraph Game Client
        Client["<b>Merciless Warrior</b><br><i>Java Swing/FX Game Client<br>"]
    end

    subgraph "Backend Microservices (Dockerized)"
        APIGW["<b>API Gateway</b><br>Routes external requests"]
        Registry["<b>Service Registry</b><br>Netflix Eureka"]
        
        subgraph "Business Services"
            AuthSvc["<b>Authentication Service</b><br>Handles users"]
            GameSvc["<b>Game Service</b><br>Handles game data"]
        end

        subgraph "Data Stores"
            AuthDB[("💽<br>Auth DB<br><i>(PostgreSQL)</i>")]
            GameDB[("💽<br>Game DB<br><i>(PostgreSQL)</i>")]
            Redis[("⚡️<br>Redis<br><i>(Cache & Rate Limiting)</i>")]
        end
    end

    Player -- Plays --> Client

    Client -- "HTTP API Calls<br>(Login, Save, Leaderboard)" --> APIGW

    APIGW -- "Routes /auth/**" --> AuthSvc
    APIGW -- "Routes /game/**, /items/**, etc." --> GameSvc

    AuthSvc <-->|Reads/Writes Users & Roles| AuthDB
    GameSvc <-->|Reads/Writes Game State| GameDB
    
    AuthSvc <-->|Rate Limiting<br>Login Attempts| Redis
    GameSvc <-->|Resilience Cache| Redis
    
    GameSvc -- "Internal API Calls" --> AuthSvc
    
    APIGW -.->|Registers & Discovers| Registry
    AuthSvc -.->|Registers & Discovers| Registry
    GameSvc -.->|Registers & Discovers| Registry
    
    style Player fill:#3498DB,stroke:#2C3E50,stroke-width:2px,color:#FFFFFF
    style Client fill:#2980B9,stroke:#2C3E50,stroke-width:2px,color:#FFFFFF
    style APIGW fill:#16A085,stroke:#2C3E50,stroke-width:2px,color:#FFFFFF
    style AuthSvc fill:#E67E22,stroke:#2C3E50,stroke-width:2px,color:#FFFFFF
    style GameSvc fill:#E67E22,stroke:#2C3E50,stroke-width:2px,color:#FFFFFF
    style Registry fill:#7F8C8D,stroke:#2C3E50,stroke-width:2px,color:#FFFFFF
    style AuthDB fill:#95A5A6,stroke:#2C3E50,stroke-width:2px,color:#FFFFFF
    style GameDB fill:#95A5A6,stroke:#2C3E50,stroke-width:2px,color:#FFFFFF
    style Redis fill:#95A5A6,stroke:#2C3E50,stroke-width:2px,color:#FFFFFF
```

## Getting Started

Follow these steps to get a local copy up and running.

### Prerequisites

- **Java Development Kit (JDK) 17** or newer.
- **Docker** and **Docker Compose**.
- An IDE that supports Java/Gradle projects (e.g., IntelliJ IDEA).

### Installation & Launch

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/VasilijeJukic01/MercilessWarrior-Java.git
    cd MercilessWarrior-Java
    ```

2.  **Start the Backend Services:**
    Open a terminal in the root directory and run the following Docker Compose command. This will build the images for each microservice and start them, along with the PostgreSQL databases and Redis instance.
    ```sh
    docker-compose up --build
    ```
    Wait for all the services to start up.

3.  **Run the Game Client:**
  - Open the `core` directory as a project in your IDE.
  - Let the IDE sync the Gradle dependencies.
  - Locate and run the `Launcher.java` file (`core/src/main/java/platformer/launcher/Launcher.java`).

    The game launcher should now appear, allowing you to start playing!

## Acknowledgements

- **My Friends:** This project was a solo development effort, but it would not have been the same without the invaluable ideas, feedback, and encouragement from my friends. Your creativity helped shape the world of "Merciless Warrior." A special thank you to:
  - **[Danilo J.]**
  - **[Stevan B.]**
  - **[Jovan P.]**
  - **[Mehmedalija K.]**
  - **[Marija P.]**

- **Asset Credits:**
  - **Art:** [Dreamir](https://dreamir.itch.io/), [Maaot](https://maaot.itch.io/), [brullov](https://brullov.itch.io/), [CreativeKind](https://creativekind.itch.io/)
  - **Music:** Matthias Verbinnen

## License

This project is licensed under the [AGPLv3 License](LICENSE).