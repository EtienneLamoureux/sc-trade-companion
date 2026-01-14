# SC Trade Companion
![Build status](https://github.com/EtienneLamoureux/sc-trade-companion/actions/workflows/gradle.yml/badge.svg?branch=main)

Desktop companion application for Star Citizen.

## Features
- Automatic commodity kiosk scraping by pressing the <kbd>F3</kbd> key
- All extracted data saved to the `/my-data` folder as CSV files
- All screenshots saved to the `/my-images` folder
- Available in: 
  - 🇬🇧 English
  - 🇫🇷 French
  - 🇩🇪 German
  - 🇧🇷 Brazilian Portuguese
  - 🇪🇸 Spanish
- [Known issues](https://github.com/EtienneLamoureux/sc-trade-companion/issues?q=is%3Aopen+is%3Aissue+label%3Abug)

## Installation
1. Download the [latest release](https://github.com/EtienneLamoureux/sc-trade-companion/releases) `.exe` file, under the "Assets" section
1. Extract the application wherever

### Uninstalling
1. Delete the `SC Trade Companion` folder wherever you installed it
1. Done!

## Usage
1. Start the application by double-clicking the `sc-trade-companion.bat` file
1. Play Star Citizen, on LIVE, in English
1. When you get to a commodity kiosk, interact with it
    1. To ensure good results, check-out the [best practices](https://github.com/EtienneLamoureux/sc-trade-companion#best-practices)
1. In the left "Your inventories" dropdown, select the shop
1. For the `Buy` tab:
    1. Press <kbd>F3</kbd>
    1. Scroll and repeat in order to capture all listings
1. Repeat for the `Sell` tab
1. Repeat throughout your play session
1. Close the application (make sure you close it in the system tray)

### Best practices
1. Align your character **as close and as straight** to the screen as possible
1. Turn off hints (Options > Game settings > Show Hints, Control Hints)
1. Turn off global chat (<kbd>F12</kbd> by default)
1. If you use `r_DisplayInfo`, make sure the text doesn’t intrude on the screen
1. Try to minimize glare, by positioning your character or by adjusting the "Gamma", "Brightness" and "Contrast" in the "Graphics" options

## F.A.Q.
### Why would I use this?
It auto-scrapes commodity kiosk terminals at the press of a button, and gives you all that data in CSV format! It also helps the Star Citizen community at large by contributing to a shared pool of information.

### Where are my screenshots processed?
Locally, on your computer. 

### Is any data transmitted to a third-party?
We take your privacy very seriously. The only data sent out are the final, parsed commodity listings. They are published to [SC Trade Tools](https://sc-trade.tools). 

## Contributing
See our [contribution guidelines](./CONTRIBUTING.md)

## Disclaimer
<sup>SC Trade Companion is an unofficial Star Citizen application, not affiliated with the Cloud Imperium group of companies. All content on SC Trade Companion not authored by its host or users are property of their respective owners. Star Citizen®, Roberts Space Industries® and Cloud Imperium® are registered trademarks of Cloud Imperium Rights LLC</sup>
