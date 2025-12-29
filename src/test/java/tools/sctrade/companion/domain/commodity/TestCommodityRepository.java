package tools.sctrade.companion.domain.commodity;

import java.util.Arrays;
import java.util.List;

class TestCommodityRepository implements CommodityRepository {

  @Override
  public List<String> findAllCommodities() {
    return Arrays.asList("AcryliPlex Composite", "Agricium", "Agricium (Ore)",
        "Agricultural Supplies", "Altruciatoxin", "Aluminum", "Aluminum (Ore)", "Amioshi Plague",
        "Ammonia", "Aphorite", "Apoxygenite", "Argon", "Astatine", "Atlasium", "Beradom", "Beryl",
        "Beryl (Raw)", "Bexalite", "Bexalite (Raw)", "Bioplastic", "Blue Bilva", "Borase",
        "Borase (Ore)", "CK13-GID Seed Blend", "Carbon", "Carbon-Silk", "Carinite", "Chlorine",
        "Cobalt", "Compboard", "Construction Materials", "Copper", "Copper (Ore)", "Corundum",
        "Corundum (Raw)", "DCSR2", "Decari Pod", "Degnous Root", "Detatrine", "Diamond",
        "Diamond (Raw)", "Diamond Laminate", "Diluthermex", "Distilled Spirits", "Dolivine",
        "Dopple", "Dymantium", "DynaFlex", "E'tam", "Elespo", "Feynmaline", "Fireworks", "Fluorine",
        "Fotia Seedpod", "Freeze", "Fresh Food", "Gasping Weevil Eggs", "Glacosite",
        "Glacosite (Raw)", "Glow", "Gold", "Gold (Ore)", "Golden Medmon", "HLX99 Hyperprocessors",
        "Hadanite", "Heart of the Woods", "Helium", "Hephaestanite", "Hephaestanite (Raw)",
        "HexaPolyMesh Coating", "Human Food Bars", "Hydrogen", "Hydrogen Fuel", "Inert Materials",
        "Iodine", "Iron", "Iron (Ore)", "Jaclium (Ore)", "Janalite", "Jumping Limes", "Kopion Horn",
        "Laranite", "Laranite (Raw)", "Lastaprene", "Lindinium", "Lindinium (Ore)",
        "Luminalia Gift", "Lunes (Spiral Fruit)", "Lycara", "Mala", "Marok Gem", "Maze", "MedPens",
        "Medical Supplies", "Mercury", "Methane", "Neograph", "Neon", "Nitrogen", "Omnapoxy",
        "Organics", "Osoian Hides", "OxyPens", "Oza", "Partillium", "Pingala Seeds", "Pitambu",
        "Potassium", "Pressurized Ice", "Processed Food", "Prota", "Quantainium",
        "Quantainium (Raw)", "Quantum Fuel", "Quartz", "Quartz (Raw)", "RS1 Odysey Spacesuits",
        "Ranta Dung", "Raw Ice", "Recycled Material Composite", "Redfin Energy Modulators",
        "Revenant Pod", "Revenant Tree Pollen", "Riccite", "SLAM", "Saldynium (Ore)", "Sarilus",
        "Savrilium", "Savrilium (Ore)", "Scrap", "Ship Ammunition", "Silicon", "Silnex",
        "Souvenirs", "Steel", "Stileron", "Stims", "Stone Bug Shell", "Sunset Berries", "Taranite",
        "Taranite (Raw)", "ThermalFoam", "Thrust", "Tin", "Titanium", "Titanium (Ore)", "Torite",
        "Tritium", "Tungsten", "Tungsten (Ore)", "Uncut SLAM", "Waste", "WiDoW", "Xa'Pyen",
        "Year of the Dog Envelope", "Zeta-Prolanide", "Zip", "mobyGlass Personal Computers");
  }
}
