package tools.sctrade.companion.domain.commodity;

import java.util.Arrays;
import java.util.List;
import tools.sctrade.companion.domain.LocationRepository;

class TestLocationRepository implements LocationRepository {
  @Override
  public List<String> findAllLocations() {
    return Arrays.asList("Shubin Mining Facility SAL-5", "ARC-L2 Lively Pathway Station",
        "HUR-L5 High Course Station", "ARC-L4 Faint Glen Station",
        "HUR-L3 Thundering Express Station", "Raven's Roost", "Orison", "Area18", "New Babbage",
        "Rayari McGrath Research Outpost", "Tram & Myers Mining", "Terra Mills HydroFarm",
        "ARC-L1 Wide Forest Station", "HDMS-Lathan", "Grim HEX", "Jumptown", "HDMS-Stanhope",
        "ArcCorp Mining Area 056", "Devlin Scrap & Salvage", "HDMS-Hadley",
        "HUR-L1 Green Glade Station", "HDMS-Pinewood", "HDMS-Oparei", "HDMS-Woodruff",
        "Shubin Mining Facility SM0-22", "Reclamation & Disposal Orinth", "HDMS-Norgaard",
        "The Orphanage", "CRU-L4 Shallow Fields Station", "Shubin Mining Facility SCD-1",
        "ArcCorp Mining Area 048", "Benson Mining Outpost", "Kudre Ore", "HDMS-Anderson",
        "HUR-L2 Faithful Dream Station", "ArcCorp Mining Area 045", "Paradise Cove", "HDMS-Hahn",
        "Shubin Mining Facility SM0-18", "Pyro Gateway", "HDMS-Bezdek",
        "Rayari Deltana Research Outpost", "Shubin Mining Facility SM0-10",
        "CRU-L5 Beautiful Glen Station", "MIC-L2 Long Forest Station",
        "Shubin Mining Facility SM0-13", "HDMS-Perlman", "Outpost 54", "The Necropolis",
        "Seraphim Station", "Deakins Research Outpost", "Port Tressler", "HDMS-Thedus",
        "MIC-L4 Red Crossroads Station", "Everus Harbor", "MIC-L5 Modern Icarus Station",
        "HUR-L4 Melodic Fields Station", "ArcCorp Mining Area 157", "Nuen Waste Management",
        "CRU-L1 Ambitious Dream Station", "Humboldt Mines", "HDMS-Ryder",
        "Loveridge Mineral Reserve", "Rayari Anvik Research Outpost",
        "Samson & Son's Salvage Center", "Baijini Point", "Bountiful Harvest Hydroponics",
        "Gallete Family Farms", "Hickes Research Outpost", "Rayari Cantwell Research Outpost",
        "MIC-L1 Shallow Frontier Station", "ARC-L5 Yellow Core Station", "Lorville",
        "ARC-L3 Modern Express Station", "HDMS-Edmond", "Brio's Breaker Yard", "Shady Glen Farms",
        "Rayari Kaltag Research Outpost", "NT-999-XX", "Shubin Mining Facility SMCa-6",
        "Bud's Growery", "MIC-L3 Endless Odyssey Station", "ArcCorp Mining Area 061",
        "ArcCorp Mining Area 141", "PRIVATE PROPERTY", "Terra Gateway",
        "Shubin Mining Facility SAL-2", "Magnus Gateway", "Shubin Mining Facility SMCa-8",
        "Dunboro", "Maker's Point", "Rappel", "Pickers Field", "Astor’s Clearing", "Seer's Canyon",
        "Ashland", "Kabir's Outpost", "Chawla's Beach", "Sacren's Plot", "Fallow Field",
        "Goner's Deal", "Endgame", "Frigid Knot", "Bueno Ravine", "The Golden Riviera", "Rustville",
        "Blackrock Exchange", "Canard View", "Last Landings", "Rough Landing", "Ruin Station",
        "Starlight Service Station", "Patch City", "Rod's Fuel 'N Supplies", "Megumi Refueling",
        "Checkmate", "Rat's Nest", "Gaslight", "Dudley & Daughters", "Arid Reach", "Jackson's Swap",
        "Sunset Mesa", "Orbituary", "Baijini Point Platinum Bay", "CRU-L4 Locker Room",
        "CRU-L5 Maintenance Area", "CRU-L5 Platinum Bay", "Everus Harbor Platinum Bay",
        "HUR-L5 Platinum Bay", "Providence Platform", "Port Tressler Platinum Bay",
        "ARC-L1 Platinum Bay", "Area18 Dumper's Depot", "IO-North Tower", "CRU-L1 Platinum Bay",
        "CRU-L4 Platinum Bay", "Grim HEX Dumper's Depot", "HUR-L2 Platinum Bay",
        "HUR-L3 Platinum Bay", "Leavsden Square Admin Office", "Magnus Gateway Platinum Bay",
        "MT Planetary Services", "Pyro Gateway Platinum Bay", "Terra Gateway Platinum Bay",
        "Stanton Gateway", "Stanton Gateway Platinum Bay", "Shepherd's Rest",
        "Stanton Gateway Platinum Bay", "Pyro Gateway", "Pyro Gateway Platinum Bay",
        "Stanton Gateway", "Nyx Gateway Platinum Bay", "Levski", "Stanton Gateway");
  }

}
