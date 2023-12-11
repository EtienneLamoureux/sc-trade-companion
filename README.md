# SC Trade Companion
Desktop companion application for Star Citizen.

## Features
- Automatic commodity kiosk scraping by pressing the <kbd>F3</kbd> key
- All extracted data saved to the `/my-data` folder as CSV files
- All screenshots saved to the `/my-images` folder
- Available in: 
  - 🇬🇧 English
  - 🇫🇷 French
- [Known issues](https://github.com/EtienneLamoureux/sc-trade-companion/issues?q=is%3Aopen+is%3Aissue+label%3Abug)

## Installation
1. Download the [latest release](https://github.com/EtienneLamoureux/sc-trade-companion/releases) `.exe` file, under the "Assets" section
2. Extract the application wherever

### Uninstalling
1. Delete the `SC Trade Companion` folder wherever you installed it
2. Done!

## Usage
1. Start the application by double-clicking the `sc-trade-companion.bat` file
2. Play Star Citizen, on LIVE, in English
3. When you get to a commodity kiosk, interact with it
    1. To ensure good results, check-out the [best practices](https://github.com/EtienneLamoureux/sc-trade-companion#best-practices)
4. For the `Buy` tab:
    1. Make the listings "not red" by selecting your ship in the "Your inventories" dropdown
    2. Press <kbd>F3</kbd>
    3. Scroll and repeat in order to capture all listings
5. For the `Sell` tab
    1. In the left “Your inventories” dropdown, select the shop
    2. Press <kbd>F3</kbd>
    3. Scroll and repeat in order to capture all listings
6. Repeat throughout your play session
7. Close the application (make sure you close it in the system tray)

### Best practices
1. Align your character **as close and as straight** to the screen as possible
2. Turn off hints (Options > Game settings > Show Hints, Control Hints)
3. Turn off global chat (<kbd>F12</kbd> by default)
4. If you use `r_DisplayInfo`, make sure the text doesn’t intrude on the screen
5. Try to minimize glare

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
