# Merciless Warrior: Java Swing 2D Platformer Game

<p float="left">
<img src="images/gif1.gif" alt="Gif1" width="49%">
<img src="images/gif2.gif" alt="Gif2" width="49%">
</p>

## Table of Contents

- [Description](#description)
- [Game Features](#game-features)
- [Backend Overview](#backend-overview)
- [Design Patterns Used](#design-patterns-used)
- [Gameplay](#gameplay)
- [Installation](#installation)
- [License](#license)

## Description

This is a 2D platformer game developed using Java Swing and Spring framework without the use of a game engine. The game incorporates various features such as enemy interactions, coin and token collection for upgrades, an open-world structure allowing backtracking to previous levels, boss fights, destructible objects, and item drops.

## Game Features

- Adjustable Resolution.
- Engaging 2D platformer gameplay.
- Diverse enemy interactions and multiple methods for eliminating them.
- Coin and token collection mechanics for upgrades.
- Buying, selling, crafting and collecting items.
- Open-world design enabling exploration of previous levels.
- Challenging boss fight for added excitement.
- Destructible objects that yield various items.
- Multiple sounds support (sfx, ambience and music) powered by OpenAL library.
- Talk to various NPCs.
- Save and load game mechanism, both locally on the computer and in a cloud.
- Leaderboard system

## Backend Overview

Backed is developed in Spring framework using Kotlin. It consists of four services:
- ```Authentication Service``` - Responsible for user authentication and authorization.
- ```Game Service``` - Handles game-related operations.
- ```Service Registry``` - Acts as a centralized registry for all services in the system.
- ```API Gateway``` - Acts as a single entry point for all client requests and routes requests to appropriate services.


## Design Patterns Used
- Singleton <br>
  Used to create a global instance for essential components such as Audio and Framework.
- State <br>
  Used to toggle between different game states.
- Observer <br>
  Used in creating a flexible logger that reacts to changes in the game.
- Bridge <br>
  Used for smooth game integration with the Spring backend.
- Facade <br>
  Used to simplify the implementation of complex functionalities across classes.
- Strategy <br>
  Used to dynamically switch between different rendering approaches.
- Flyweight <br>
  Used to optimize RAM usage during particle rendering.

## Gameplay

Navigate through various levels, engaging in combat with enemies using a variety of methods. Collect coins and tokens to unlock character upgrades, enhancing abilities and equipment. Experience intense boss fights that test your skills. The open-world structure allows you to backtrack and explore previous levels, uncovering hidden secrets and improving your score.

## Installation

1. Clone this repository.
2. Ensure you have Java Development Kit (JDK 10.0.2) and Docker installed.
3. Open command line interface and navigate to the root of the project.
4. Build and run docker images with `docker-compose up --build`.
5. Compile and run the game using your preferred Java IDE.

## License

This project is licensed under the [MIT License](LICENSE).

---
