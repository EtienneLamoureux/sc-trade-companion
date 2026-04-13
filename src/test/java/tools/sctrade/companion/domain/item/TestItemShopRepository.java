package tools.sctrade.companion.domain.item;

import java.util.List;

public class TestItemShopRepository implements ItemShopRepository {

  @Override
  public List<String> findAllTypes() {
    return List.of("Armor", "Cargo Services", "Casaba Outlet", "CenterMass", "Clothing",
        "Cubby Blast", "Dumper's Depot", "Hurston Dynamics", "Kel.To", "Live Fire Weapons",
        "Medical_Shop", "Pharmacy", "Platinum Bay", "Refinery Shop", "Ship Weapons",
        "Shop_Terminal", "Tammany and Sons", "Weapons_shop");
  }
}
