package tools.sctrade.companion.domain.gamelog.lineprocessors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import tools.sctrade.companion.domain.commodity.CommodityListingFactory;
import tools.sctrade.companion.domain.commodity.CommodityService;
import tools.sctrade.companion.domain.notification.NotificationService;

class LoadShopInventoryDataLogLineProcessorTest {
  @Mock
  private CommodityListingFactory commodityListingFactory;
  @Mock
  private CommodityService commodityService;
  @Mock
  private NotificationService notificationService;

  private LoadShopInventoryDataLogLineProcessor processor;

  @BeforeEach()
  void setUp() {
    processor = new LoadShopInventoryDataLogLineProcessor(commodityListingFactory, commodityService,
        notificationService);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "<2024-12-04T19:23:39.548Z> [Notice] <CEntityComponentCommodityUIProvider::LoadShopInventoryData::<lambda_1>::operator ()> AddingCommodityBox - playerId[123456789012] shopId[5643020808917] shopName[SCShop_CommEx_TDD_Orison] commodityName[ResourceType.Scrap] Available Box Sizes:  boxSize[1] boxSize[2] boxSize[4] boxSize[8] boxSize[16] boxSize[24] boxSize[32] [Team_NAPU][Shops][UI]",
      "<2024-12-04T19:31:12.471Z> [Notice] <CEntityComponentCommodityUIProvider::LoadShopInventoryData::<lambda_1>::operator ()> AddingCommodityBox - playerId[123456789012] shopId[5643020760642] shopName[SCShop_AdminOffice_NewBabbage-002] commodityName[ResourceType.Scrap] Available Box Sizes:  boxSize[1] boxSize[2] boxSize[4] boxSize[8] boxSize[16] boxSize[24] boxSize[32] [Team_NAPU][Shops][UI]"})
  void givenValidLogLinesWhenCheckingIfCanHandleThenReturnTrue(String line) {
    // TODO
  }
}
