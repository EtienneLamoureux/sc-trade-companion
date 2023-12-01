package tools.sctrade.companion.domain.commodity;

import java.util.Arrays;
import java.util.List;

class TestCommodityRepository implements CommodityRepository {

  @Override
  public List<String> findAll() {
    return Arrays.asList("AcryliPlex Composite", "Agricium", "Agricium (Ore)",
        "Agricultural Supplies", "Altruciatoxin", "Aluminum", "Aluminum (Ore)", "Amioshi Plague",
        "Aphorite", "Astatine", "Atlasium", "Beryl", "Beryl (Raw)", "Bexalite", "Bexalite (Raw)",
        "Borase", "Borase (Ore)", "Chlorine", "Compboard", "Copper", "Copper (Ore)", "Corundum",
        "Corundum (Raw)", "Degnous Root", "Diamond", "Diamond (Raw)", "Diluthermex",
        "Distilled Spirits", "Dolivine", "Dymantium", "E'tam", "Fluorine", "Gold", "Gold (Ore)",
        "Golden Medmon", "Hadanite", "Heart of the Woods", "Hephaestanite", "Hephaestanite (Raw)",
        "Hydrogen", "Inert Materials", "Iodine", "Iron", "Iron (Ore)", "Janalite", "Laranite",
        "Laranite (Raw)", "Maze", "Medical Supplies", "Neon", "Pitambu", "Processed Food", "Prota",
        "Quantainium", "Quantainium (Raw)", "Quartz", "Quartz (Raw)", "Ranta Dung",
        "Recycled Material Composite", "Revenant Pod", "Revenant Tree Pollen", "SLAM", "Scrap",
        "Stims", "Sunset Berries", "Taranite", "Taranite (Raw)", "Titanium", "Titanium (Ore)",
        "Tungsten", "Tungsten (Ore)", "Waste", "WiDoW", "Year of the Rooster Envelope",
        "Zeta-Prolanide");
  }
}
