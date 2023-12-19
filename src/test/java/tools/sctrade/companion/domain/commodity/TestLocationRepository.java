package tools.sctrade.companion.domain.commodity;

import java.util.Arrays;
import java.util.List;
import tools.sctrade.companion.domain.LocationRepository;

public class TestLocationRepository implements LocationRepository {

  @Override
  public List<String> findAllLocations() {
    return Arrays.asList("shubin mining facility sal-5", "\"no questions asked\" trading console",
        "arc-l2 lively pathway station", "hur-l5 high course station", "arc-l4 faint glen station",
        "hur-l3 thundering express station", "raven's roost", "orison", "area18",
        "trade and development division", "rayari mcgrath research outpost", "new babbage",
        "terra mills hydrofarm", "tram & myers mining", "arc-l1 wide forest station", "hdms-lathan",
        "grim hex", "jumptown", "hdms-stanhope", "arccorp mining area 056", "hdms-hadley",
        "devlin scrap & salvage", "hur-l1 green glade station", "hdms-pinewood", "hdms-oparei",
        "hdms-woodruff", "shubin mining facility sm0-22", "reclamation & disposal orinth",
        "hdms-norgaard", "the orphanage", "platinum bay", "cru-l4 shallow fields station",
        "arccorp mining area 048", "shubin mining facility scd-1", "benson mining outpost",
        "kudre ore", "hdms-anderson", "hur-l2 faithful dream station", "arccorp mining area 045",
        "paradise cove", "hdms-hahn", "shubin mining facility sm0-18", "stanton to pyro jump point",
        "stanton to terra jump point", "hdms-bezdek", "rayari deltana research outpost",
        "shubin mining facility sm0-10", "cru-l5 beautiful glen station",
        "mic-l2 long forest station", "shubin mining facility sm0-13", "hdms-perlman", "outpost 54",
        "the necropolis", "seraphim station", "deakins research outpost", "admin", "port tressler",
        "hdms-thedus", "mic-l4 red crossroads station", "everus harbor",
        "mic-l5 modern icarus station", "hur-l4 melodic fields station", "nuen waste management",
        "arccorp mining area 157", "cru-l1 ambitious dream station", "dumpers depot",
        "humboldt mines", "hdms-ryder", "loveridge mineral reserve",
        "rayari anvik research outpost", "samson & son's salvage center", "baijini point",
        "gallete family farms", "bountiful harvest hydroponics", "central business district",
        "hickes research outpost", "rayari cantwell research outpost", "arc-l5 yellow core station",
        "mic-l1 shallow frontier station", "lorville", "arc-l3 modern express station",
        "hdms-edmond", "brio's breaker yard", "shady glen farms", "nt-999-xx",
        "rayari kaltag research outpost", "shubin mining facility smca-6", "refinement center",
        "stanton to magnus jump point", "bud's growery", "mic-l3 endless odyssey station",
        "arccorp mining area 061", "arccorp mining area 141", "private property",
        "shubin mining facility sal-2", "shubin mining facility smca-8");
  }

}
