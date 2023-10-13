# Merciless Warrior: Java Swing 2D Platformer Game

<img src="https://i.ibb.co/qnrqcmL/Gif1.gif" alt="Game Screenshot" width="100%">
<img src="https://i.ibb.co/0BvhLKH/Gif2.gif" alt="Game Screenshot" width="100%">

## Table of Contents

- [Description](#description)
- [Features](#features)
- [Design patterns used](#design-patterns-used)
- [Gameplay](#gameplay)
- [Installation](#installation)
- [Controls](#controls)
- [Dependencies](#dependencies)
- [License](#license)

## Description

This is a 2D platformer game developed using Java Swing without the use of a game engine. The game incorporates various features such as enemy interactions, coin and token collection for upgrades, an open-world structure allowing backtracking to previous levels, boss fights, destructible objects, and item drops.

The integration of OpenAL library provides immersive sound effects, enhancing the gameplay experience. Furthermore, the MySQL connector facilitates database connectivity for managing game progress, scores, and player profiles.

## Features

- Adjustable Resolution.
- Engaging 2D platformer gameplay.
- Diverse enemy interactions and multiple methods for eliminating them.
- Coin and token collection mechanics for upgrades.
- Buying, selling, crafting and collecting items.
- Open-world design enabling exploration of previous levels.
- Challenging boss fight for added excitement.
- Destructible objects that yield various items.
- Immersive sound effects powered by OpenAL.
- Save and load game mechanism, both locally on the computer and in a database.
- Leaderboard system

## Design Patterns used
- Singleton (Thread safe)
- State
- Observer
- Bridge
- Facade
- Strategy
- Flyweight

## Gameplay

Navigate through various levels, engaging in combat with enemies using a variety of methods. Collect coins and tokens to unlock character upgrades, enhancing abilities and equipment. Experience intense boss fights that test your skills. The open-world structure allows you to backtrack and explore previous levels, uncovering hidden secrets and improving your score.

## Installation

1. Clone this repository.
2. Ensure you have Java Development Kit (JDK 10.0.2) installed.
3. Install the required dependencies (see [Dependencies](#dependencies)).
4. Set up the MySQL database using the provided configuration file (contact me for database credentials).
5. Add `lib` folder, which contains DLL files, to dependencies.
6. Compile and run the game using your preferred Java IDE.

## Controls

- **Left Arrow:** Move left.
- **Right Arrow:** Move right.
- **Up Arrow:** Jump/Double jump.
- **X:** Attack.
- **C:** Spell attack.
- **S:** Block attack.
- **V:** Dash.
- **Q:** Transform.
- **F:** Interact.
- **I:** Inventory.
- **ESC:** Pause menu.

## Dependencies

- Java Swing
- OpenAL library for Java
- MySQL Connector
- GSON

## License

This project is licensed under the [MIT License](LICENSE).

---
