# OrleansRealms

OrleansRealms is a Minecraft Paper plugin developed in Java for the OrleansMC server network. It provides a
comprehensive realm system, allowing players to create, manage, and explore custom realms with unique climates, member
management, and progression features. This repository is released for transparency and community use following the
shutdown of the original OrleansMC server.

> **Note:** This project was not originally designed as an open source project. Some features may be incomplete, tightly
> coupled to the original infrastructure, or lack full documentation.

> 📘 **Looking for a visual and detailed feature guide?**
> Check out our comprehensive guides on the OrleansMC website:
> 🗣️ _These guides are written in Turkish._
> - [İşte Başlıyoruz](https://orleansmc.com/rehber/iste-basliyoruz)
> - [Diyar Oluşturma](https://orleansmc.com/rehber/diyar-olusturma)
> - [Orleans Mücevheri](https://orleansmc.com/rehber/orleans-mucevheri)

---

## Table of Contents

- [Project Purpose and Scope](#project-purpose-and-scope)
- [Key Features](#key-features)
- [Technologies Used](#technologies-used)
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Development Notes](#development-notes)
- [Limitations](#limitations)
- [License](#license)
- [Acknowledgements](#acknowledgements)

---

## Project Purpose and Scope

OrleansRealms was developed to power the realm gameplay on the OrleansMC Minecraft server, providing a system for
players to create and manage their own worlds ("realms") with unique biomes, climates, and progression. The plugin is
now open-sourced for the community to learn from, adapt, or extend.

The scope of the project includes:

- Realm creation with climate/biome selection
- Realm member management and permissions
- Realm progression, upgrades, and settings
- Integration with external plugins (Quests, PlaceholderAPI, LuckPerms, ItemsAdder)
- Custom menus and GUIs for realm management
- Support for deterministic biome assignment via OrleansGenerator

---

## Key Features

- **Custom Realms:** Players can create their own worlds ("realms") with selectable climates and biomes.
- **Biome/Climate Assignment:** Uses a fork of TerraformGenerator (OrleansGenerator) to deterministically assign biomes
  based on realm location.
- **Member Management:** Invite, promote, demote, or remove members from your realm.
- **Realm Progression:** Realms can be upgraded, expanded, and customized.
- **Quest Integration:** Supports custom objectives for realm creation via the Quests plugin.
- **GUI Menus:** Intuitive in-game menus for managing realms, members, and settings.
- **External Plugin Integration:** Works with PlaceholderAPI, LuckPerms, ItemsAdder, and more.

---

## Technologies Used

- **Java 21**
- **Paper API 1.21.1**
- **MongoDB (via Lucko's Helper library)**
- **Quests** (for quest objectives)
- **PlaceholderAPI** (for dynamic placeholders)
- **LuckPerms** (for permissions and chat prefixes)
- **ItemsAdder** (for custom items and GUIs)
- **OrleansGenerator** (fork of TerraformGenerator for biome assignment)

---

## Installation

1. **Requirements:**
    - Minecraft server running [Paper 1.21.1](https://papermc.io/downloads)
    - Java 21 or newer
    - MongoDB server (for player and realm data)
    - [Lucko's Helper library](https://github.com/lucko/helper)
    - [Quests plugin](https://github.com/PikaMug/Quests)
    - [LuckPerms](https://luckperms.net/)
    - [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
    - [ItemsAdder](https://itemsadder.devs.beer/)
    - [OrleansGenerator](https://github.com/OrleansMC/OrleansGenerator) (see below)

2. **Dependencies:**
    - Build the latest `OrleansMC-0.1-all.jar` from [OrleansMC](https://github.com/OrleansMC/OrleansMC) core plugin and
      place it in the `lib` directory.
    - Download the Quests plugin JAR and place it in the `lib` directory.
    - Download and install [OrleansGenerator](https://github.com/OrleansMC/OrleansGenerator) in your server's `plugins`
      directory.

3. **Build the Plugin:**
    - Clone this repository.
    - Run `./gradlew build` to generate the plugin JAR.
    - The compiled JAR will be located in the `build/libs` directory.

4. **Install the Plugin:**
    - Place the JAR file into your server's `plugins` directory.
    - Ensure all required dependencies are also installed.

5. **Start the Server:**
    - Start or restart your Paper server to load the plugin.

---

## Usage

- The plugin will automatically create default configuration files (`config.yml`, `texts.yml`, etc.) in the plugin's
  data folder on first run.
- Players can create a realm using in-game menus or commands.
- Realms are assigned a climate/biome based on player selection and deterministic region logic (see OrleansGenerator).
- Use the provided GUIs to manage members, settings, and upgrades.
- Administrators can manage realms and player data via commands or the database.

---

## Configuration

The main configuration files are `config.yml` and `texts.yml`. Key settings include:

- **Database Settings:** Configure MongoDB connection for realm and player data.
- **Menu and GUI Settings:** Customize menu layouts and item appearances.
- **Texts and Localization:** Edit `texts.yml` for in-game messages and localization.

Example `config.yml`:

```yaml
# The name of the server (e.g., realms-1)
server-name: '{SERVER_NAME}'

# The name of the world (e.g., realms_world)
world-name: '{WORLD_NAME}'

realm-radius:
  # Default radius for a new realm
  default: 75
  # Price to increase radius to 150 (in gems)
  increase-150-price: 7500
  # Price to increase radius to 250 (in credits)
  increase-250-price: 11400
  # Price to increase radius to 500 (in credits)
  increase-500-price: 45000

realm-member-count:
  # Default allowed member count per realm
  default: 3
  # Price to increase member count (in credits)
  increase-price: 500

# Price to teleport back to death location (in gems)
back-to-death-location-price: 75

# Webhook URLs (replace with your own Discord webhook URLs)
realm-webhook-url: "https://discord.com/api/webhooks/..."
redstone-alert-webhook-url: "https://discord.com/api/webhooks/..."
death-webhook-url: "https://discord.com/api/webhooks/..."
```

---

## OrleansGenerator and Biome Assignment

OrleansRealms uses [OrleansGenerator](https://github.com/OrleansMC/OrleansGenerator), a fork of
Hex27/TerraformGenerator, to assign biomes and climates to realms. The generator deterministically assigns biome
climates based on the sign and region of x-z coordinates, dividing the world into quadrants and regions. This allows
players to create realms in different biomes by choosing their x-z coordinates or climate during creation.

**Climates supported:**

- HOT_BARREN
- DRY_VEGETATION
- COLD
- HUMID_VEGETATION
- SNOWY

---

## ItemsAdder Integration

The plugin requires specific ItemsAdder assets to function correctly. Make sure the following models, items, and fonts
are present in your ItemsAdder configuration:

### Font Images

- `ui:quests_menu`
- `ui:parchment`
- `ui:parchment_ticked`
- `ui:locked`
- `ui:icon_next_purple`
- `ui:icon_back_purple`
- `ui:realm_border`
- `ui:realm_border_gem`
- `ui:realm_border_credit`
- `ui:realm_border_char_dot`
- `ui:realm_border_digit_0` ... `ui:realm_border_digit_9`
- `ui:realm_members`
- `ui:realm_members_credit`
- `ui:realm_members_char_dot`
- `ui:realm_members_digit_0` ... `ui:realm_members_digit_9`

### Nameplate/Tags

- `%img_realm_owner_nameplate%`
- `%img_realm_manager_nameplate%`
- `%img_realm_worker_nameplate%`
- `%img_realm_member_nameplate%`
- `%img_realm_visitor_nameplate%`

> **Note:** Ensure all IDs match exactly as referenced in the plugin code and menus.

---

## Development Notes

- **Original Design:** The codebase was not originally intended for open source release. Some parts may be tightly
  coupled, lack modularity, or have minimal documentation.
- **Extending Functionality:** Contributions are welcome, but please be aware of the architectural limitations.
- **Dependencies:** The project relies on several external plugins and services. Ensure all dependencies are installed
  and configured.

---

## Limitations

- **Not Fully Modular:** Some features may be hardcoded or not easily configurable for other server networks.
- **Documentation:** Inline code documentation may be sparse or missing.
- **Feature Completeness:** Certain features may be incomplete or specific to the original OrleansMC server.
- **No Official Support:** This project is provided as-is, with no guarantee of support or updates.

---

## License

Unless otherwise specified, this project is licensed under the Apache License 2.0.  
See the [`LICENSE`](./LICENSE.txt) file for details.

## Acknowledgements

- [OrleansMC Core](https://github.com/OrleansMC/OrleansMC)
- [OrleansGenerator](https://github.com/OrleansMC/OrleansGenerator)
- [Quests](https://github.com/PikaMug/Quests)
- [Lucko's Helper](https://github.com/lucko/helper)
- [LuckPerms](https://luckperms.net/)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
- [ItemsAdder](https://itemsadder.devs.beer/)
- The original OrleansMC community

---

For questions, issues, or contributions, please use the GitHub Issues and Pull Requests features.

---