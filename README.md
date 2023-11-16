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
1. Install Java 17+
    * To verify if you already have Java 17+, execute the following:
       1. <kbd>Windows key</kbd>+<kbd>R</kbd>
       2. Type `cmd`, then press <kbd>Enter</kbd>
       3. Type in `java -version`, then press <kbd>Enter</kbd>
       4. If an error occurs or the version listed is not 17+, please [install Java 17+](https://www.oracle.com/java/technologies/downloads/)
3. Download the [latest release](https://github.com/EtienneLamoureux/sc-trade-companion/releases) `.exe` file, under the "Assets" section
4. Extract the application wherever

### Uninstalling
1. Delete the `SC Trade Companion` folder wherever you installed it
2. Done!

## Usage
1. Start the application by double-clicking the `SC Trade Companion/sc-trade-companion.bat` file
2. Play Star Citizen
3. When you get to a commodity kiosk, interact with it
4. Align your character as close and as straight to the screen as possible
5. Turn off global chat (<kbd>F12</kbd> by default)
6. Go to the "Buy" tab
7. In the left "Your inventories" dropdown, select your ship
8. Press <kbd>F3</kbd>, scroll, then press <kbd>F3</kbd> again. Repeat in order to capture all listings.
9. Go to the "Sell" tab
10. In the left "Your inventories" dropdown, select the shop
8. Press <kbd>F3</kbd>, scroll, then press <kbd>F3</kbd> again. Repeat in order to capture all listings.
12. Repeat throughout your play session
13. Close the application (make sure you close it in the system tray)

## F.A.Q.
### Why would I use this?
It auto-scrapes commodity kiosk terminals at the press of a button, and gives you all that data in CSV format! It also helps the Star Citizen community at large by contributing to a shared pool of information.

### Where are my screenshot processed?
Locally, on your computer. 

### Is any data transmitted to a third-party?
We take your privacy very seriously. The only data sent out are the final, parsed commodity listings. They are published to [SC Trade Tools](https://sc-trade.tools). 

## Contributing
See our [contribution guidelines](./CONTRIBUTING.md)

## Disclaimer
<sup>SC Trade Companion is an unofficial Star Citizen application, not affiliated with the Cloud Imperium group of companies. All content on SC Trade Tools not authored by its host or users are property of their respective owners. Star Citizen®, Roberts Space Industries® and Cloud Imperium® are registered trademarks of Cloud Imperium Rights LLC</sup>
