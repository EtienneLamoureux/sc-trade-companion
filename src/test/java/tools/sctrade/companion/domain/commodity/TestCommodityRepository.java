package tools.sctrade.companion.domain.commodity;

import java.util.Arrays;
import java.util.List;

class TestCommodityRepository implements CommodityRepository {

  @Override
  public List<String> findAllCommodities() {
    return Arrays.asList("acryliplex composite", "agricium", "agricium (ore)",
        "agricultural supplies", "altruciatoxin", "aluminum", "aluminum (ore)", "amioshi plague",
        "aphorite", "astatine", "atlasium", "beryl", "beryl (raw)", "bexalite", "bexalite (raw)",
        "borase", "borase (ore)", "chlorine", "compboard", "copper", "copper (ore)", "corundum",
        "corundum (raw)", "degnous root", "diamond", "diamond (raw)", "diluthermex",
        "distilled spirits", "dolivine", "dymantium", "e'tam", "fluorine", "gold", "gold (ore)",
        "golden medmon", "hadanite", "heart of the woods", "hephaestanite", "hephaestanite (raw)",
        "hydrogen", "inert materials", "iodine", "iron", "iron (ore)", "janalite", "laranite",
        "laranite (raw)", "maze", "medical supplies", "neon", "pitambu", "processed food", "prota",
        "quantainium", "quantainium (raw)", "quartz", "quartz (raw)", "ranta dung",
        "recycled material composite", "revenant pod", "revenant tree pollen", "slam", "scrap",
        "stims", "sunset berries", "taranite", "taranite (raw)", "titanium", "titanium (ore)",
        "tungsten", "tungsten (ore)", "waste", "widow", "year of the rooster envelope",
        "zeta-prolanide");
  }
}
