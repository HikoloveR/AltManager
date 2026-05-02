# AltManager

Client-side Fabric mod for Minecraft 1.21.11 that adds a local nickname switcher to the main menu.

## Features

- Adds an `AM` button next to `Multiplayer`.
- Stores local nicknames in `config/altmanager_accounts.json`.
- Adds nicknames manually or with a random generator.
- Switches to a saved nickname with double left click.
- Highlights the selected nickname in green.
- Removes single accounts with the red `x` button.
- Clears the full account list.

## Important

This mod uses local/offline sessions only. It does not log into Microsoft accounts and does not create real online-mode authentication tokens.

## Build

```powershell
.\gradlew.bat build
```

The built mod jar will be created at:

```text
build/libs/AltManager-1.jar
```

## Installation

1. Install Fabric Loader for Minecraft 1.21.11.
2. Install Fabric API.
3. Put `AltManager-1.jar` into the Minecraft `mods` folder.

## License

All rights reserved.
