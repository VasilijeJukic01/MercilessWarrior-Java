CREATE TABLE `Users` (
  `id` integer PRIMARY KEY,
  `username` varchar(255),
  `password` varchar(255),
  `roleId` integer
);

CREATE TABLE `Roles` (
  `id` integer PRIMARY KEY,
  `name` varchar(255)
);

CREATE TABLE `Settings` (
  `id` integer PRIMARY KEY,
  `userId` integer,
  `spawnId` integer,
  `coins` integer,
  `tokens` integer,
  `exp` integer,
  `level` integer,
  `playtime` integer
);

CREATE TABLE `Perks` (
  `id` integer PRIMARY KEY,
  `settingsId` integer,
  `name` varchar(255)
);

CREATE TABLE `Items` (
  `id` integer PRIMARY KEY,
  `settingsId` integer,
  `name` varchar(255),
  `amount` int,
  `equiped` int
);

ALTER TABLE `Users` ADD FOREIGN KEY (`roleId`) REFERENCES `Roles` (`id`);

ALTER TABLE `Settings` ADD FOREIGN KEY (`userId`) REFERENCES `Users` (`id`);

ALTER TABLE `Perks` ADD FOREIGN KEY (`settingsId`) REFERENCES `Settings` (`id`);

ALTER TABLE `Items` ADD FOREIGN KEY (`settingsId`) REFERENCES `Settings` (`id`);
