package de.uniba.dsg.beverage_store.spring_boot.integration_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Bottle;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Crate;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.BottleUpdateDTO;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.CrateDTO;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.CrateUpdateDTO;
import de.uniba.dsg.beverage_store.spring_boot.properties.BottleProperties;
import de.uniba.dsg.beverage_store.spring_boot.properties.CrateProperties;
import de.uniba.dsg.beverage_store.spring_boot.repository.BottleRepository;
import de.uniba.dsg.beverage_store.spring_boot.repository.CrateRepository;
import de.uniba.dsg.beverage_store.spring_boot.service.BeverageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class BeverageControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CrateRepository crateRepository;

    @Autowired
    private BottleRepository bottleRepository;

    @Autowired
    private CrateProperties crateProperties;

    @Autowired
    private BottleProperties bottleProperties;

    @SpyBean
    private BeverageService beverageService;

    private final String CRATE_NAME = "Test Crate";
    private final String BOTTLE_NAME = "Test Bottle";
    private final String BOTTLE_SUPPLIER = "Test Supplier";
    private final String CRATE_PIC_URL = "https://www.google.com/6753651837108829.4-law.gif";
    private final String BOTTLE_PIC_URL = "https://www.google.com/6753651837108829.4-law.gif";

    private final double CRATE_PRICE = 1.0;
    private final double BOTTLE_PRICE = 1.0;

    private final double BOTTLE_VOLUME = 1.00;
    private final double BOTTLE_VOLUME_PERCENT = 0.00;

    private final int CRATE_IN_STOCK = 10;
    private final int BOTTLE_IN_STOCK = 10;

    private final int CRATE_NO_OF_BOTTLES = 10;

    private final String BASE_PATH = "/beverage";

    @Test
    public void getBottles_success() throws Exception {
        List<Bottle> mockBottles = TestHelper.getMockBottles();

        when(beverageService.getPagedBottlesWithAllowedStock(1, bottleProperties.getPageSize())).thenReturn(new PageImpl<>(mockBottles));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/bottle", TestHelper.getManager(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("bottles", mockBottles))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("beverage/bottle/list"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/bottle", TestHelper.getCustomer(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("bottles", mockBottles))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("beverage/bottle/list"));
    }

    @Test
    public void getBottles_noPageParam() throws Exception {
        List<Bottle> mockBottles = TestHelper.getMockBottles();

        when(beverageService.getPagedBottlesWithAllowedStock(1, bottleProperties.getPageSize())).thenReturn(new PageImpl<>(mockBottles));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/bottle", TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("bottles", mockBottles))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("beverage/bottle/list"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/bottle", TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("bottles", mockBottles))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("beverage/bottle/list"));
    }

    @Test
    public void getBottles_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/bottle", null, TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    public void getAddBottle_success() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/bottle/add", TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attributeExists("bottleDTO"))
                .andExpect(view().name("beverage/bottle/add"));
    }

    @Test
    public void getAddBottle_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/bottle/add", null, new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/bottle/add", TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @Transactional
    public void addBottle_success() throws Exception {
        long countBeforeAdd = bottleRepository.findAll()
                .size();

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/bottle/add", TestHelper.getManager(), getBottleDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("/**/beverage/bottle"));

        long countAfterAdd = bottleRepository.findAll()
                .size();

        assertEquals(countBeforeAdd + 1, countAfterAdd);

        Bottle addedBottle = bottleRepository.findAll()
                .stream()
                .max(Comparator.comparing(Bottle::getId))
                .orElse(null);

        assertNotNull(addedBottle);

        assertEquals(BOTTLE_NAME, addedBottle.getName());
        assertEquals(BOTTLE_PRICE, addedBottle.getPrice());
        assertEquals(BOTTLE_VOLUME, addedBottle.getVolume());
        assertEquals(BOTTLE_PIC_URL, addedBottle.getPicUrl());
        assertEquals(BOTTLE_IN_STOCK, addedBottle.getInStock());
        assertEquals(BOTTLE_SUPPLIER, addedBottle.getSupplier());
        assertEquals(BOTTLE_VOLUME_PERCENT, addedBottle.getVolumePercent());
    }

    @Test
    public void addBottle_invalidData() throws Exception {
        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/bottle/add", TestHelper.getManager(), getBottleDTOInvalidParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().hasErrors())
                .andExpect(view().name("beverage/bottle/add"));
    }

    @Test
    public void addBottle_security() throws Exception {
        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/bottle/add", null, getBottleDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/bottle/add", TestHelper.getCustomer(), getBottleDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void getEditBottle_success() throws Exception {
        Bottle mockBottle = TestHelper.getMockBottle();

        when(beverageService.getBottleById(mockBottle.getId())).thenReturn(mockBottle);

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/bottle/edit/" + mockBottle.getId(), TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("bottleId", mockBottle.getId()))
                .andExpect(MockMvcResultMatchers.model().attribute("bottleUpdateDTO", new BottleUpdateDTO(mockBottle.getName(), mockBottle.getPicUrl(), mockBottle.getPrice(), mockBottle.getVolume(), mockBottle.getVolumePercent(), mockBottle.getSupplier())))
                .andExpect(view().name("beverage/bottle/edit"));
    }

    @Test
    public void getEditBottle_bottleNotFound() throws Exception {
        long nonExistingBottleId = 0;

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/bottle/edit/" + nonExistingBottleId, TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("hasServerError", true))
                .andExpect(MockMvcResultMatchers.model().attribute("bottleId", nonExistingBottleId))
                .andExpect(MockMvcResultMatchers.model().attribute("bottleUpdateDTO", new BottleUpdateDTO()))
                .andExpect(view().name("beverage/bottle/edit"));
    }

    @Test
    public void getEditBottle_security() throws Exception {
        Bottle bottle = TestHelper.getBottle();

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/bottle/edit/" + bottle.getId(), null, new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/bottle/edit/" + bottle.getId(), TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @Transactional
    public void updateBottle_success() throws Exception {
        Bottle bottle = TestHelper.getBottle();

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/bottle/edit/" + bottle.getId(), TestHelper.getManager(), getBottleDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("/**/beverage/bottle"));

        Optional<Bottle> optionalAddedBottle = bottleRepository.findById(bottle.getId());

        assertTrue(optionalAddedBottle.isPresent());

        assertEquals(BOTTLE_NAME, optionalAddedBottle.get().getName());
        assertEquals(BOTTLE_PRICE, optionalAddedBottle.get().getPrice());
        assertEquals(BOTTLE_VOLUME, optionalAddedBottle.get().getVolume());
        assertEquals(BOTTLE_PIC_URL, optionalAddedBottle.get().getPicUrl());
        assertEquals(BOTTLE_SUPPLIER, optionalAddedBottle.get().getSupplier());
        assertEquals(BOTTLE_VOLUME_PERCENT, optionalAddedBottle.get().getVolumePercent());
    }

    @Test
    public void updateBottle_invalidData() throws Exception {
        Bottle bottle = TestHelper.getBottle();

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/bottle/edit/" + bottle.getId(), TestHelper.getManager(), getBottleDTOInvalidParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().hasErrors())
                .andExpect(MockMvcResultMatchers.model().attribute("bottleId", bottle.getId()))
                .andExpect(view().name("beverage/bottle/edit"));
    }

    @Test
    public void updateBottle_bottleNotFound() throws Exception {
        long nonExistingBottleId = 0;

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/bottle/edit/" + nonExistingBottleId, TestHelper.getManager(), getBottleDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("hasServerError", true))
                .andExpect(MockMvcResultMatchers.model().attribute("bottleId", nonExistingBottleId))
                .andExpect(view().name("beverage/bottle/edit"));
    }

    @Test
    public void updateBottle_security() throws Exception {
        Bottle bottle = TestHelper.getBottle();

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/bottle/edit/" + bottle.getId(), null, getBottleDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/bottle/edit/" + bottle.getId(), TestHelper.getCustomer(), getBottleDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void getCrates_success() throws Exception {
        List<Crate> mockCrates = TestHelper.getMockCrates();

        when(beverageService.getPagedCratesWithAllowedStock(1, crateProperties.getPageSize())).thenReturn(new PageImpl<>(mockCrates));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/crate", TestHelper.getManager(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("crates", mockCrates))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("beverage/crate/list"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/crate", TestHelper.getCustomer(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("crates", mockCrates))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("beverage/crate/list"));
    }

    @Test
    public void getCrates_noPageParam() throws Exception {
        List<Crate> mockCrates = TestHelper.getMockCrates();

        when(beverageService.getPagedCratesWithAllowedStock(1, crateProperties.getPageSize())).thenReturn(new PageImpl<>(mockCrates));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/crate", TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("crates", mockCrates))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("beverage/crate/list"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/crate", TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("crates", mockCrates))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("beverage/crate/list"));
    }

    @Test
    public void getCrates_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/crate", null, TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    public void getAddCrate_success() throws Exception {
        when(beverageService.getBottles()).thenReturn(TestHelper.getMockBottles());

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/crate/add", TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("crateDTO", new CrateDTO()))
                .andExpect(MockMvcResultMatchers.model().attribute("bottleDropdownListItems", TestHelper.getMockBottleDropdownList()))
                .andExpect(view().name("beverage/crate/add"));
    }

    @Test
    public void getAddCrate_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/crate/add", null, new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/crate/add", TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @Transactional
    public void addCrate_success() throws Exception {
        long countBeforeAdd = crateRepository.findAll()
                .size();

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/crate/add", TestHelper.getManager(), getCrateDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("/**/beverage/crate"));

        long countAfterAdd = crateRepository.findAll()
                .size();

        assertEquals(countBeforeAdd + 1, countAfterAdd);

        Crate addedCrate = crateRepository.findAll()
                .stream()
                .max(Comparator.comparing(Crate::getId))
                .orElse(null);

        assertNotNull(addedCrate);

        assertEquals(CRATE_NAME, addedCrate.getName());
        assertEquals(CRATE_PRICE, addedCrate.getPrice());
        assertEquals(CRATE_PIC_URL, addedCrate.getPicUrl());
        assertEquals(CRATE_IN_STOCK, addedCrate.getInStock());
        assertEquals(CRATE_NO_OF_BOTTLES, addedCrate.getNoOfBottles());
        assertEquals(TestHelper.getBottle().getId(), addedCrate.getBottle().getId());
    }

    @Test
    public void addCrate_bottleNotFound() throws Exception {
        when(beverageService.getBottles()).thenReturn(TestHelper.getMockBottles());

        MultiValueMap<String, String> params = getCrateDTOValidParams();
        params.put("bottleId", Collections.singletonList(String.valueOf(100)));

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/crate/add", TestHelper.getManager(), params))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("hasServerError", true))
                .andExpect(MockMvcResultMatchers.model().attribute("bottleDropdownListItems", TestHelper.getMockBottleDropdownList()))
                .andExpect(view().name("beverage/crate/add"));
    }

    @Test
    public void addCrate_invalidData() throws Exception {
        when(beverageService.getBottles()).thenReturn(TestHelper.getMockBottles());

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/crate/add", TestHelper.getManager(), getCrateDTOInvalidParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().hasErrors())
                .andExpect(MockMvcResultMatchers.model().attribute("bottleDropdownListItems", TestHelper.getMockBottleDropdownList()))
                .andExpect(view().name("beverage/crate/add"));
    }

    @Test
    public void addCrate_security() throws Exception {
        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/crate/add", null, getCrateDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/crate/add", TestHelper.getCustomer(), getCrateDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void getEditCrate_success() throws Exception {
        Crate mockCrate = TestHelper.getMockCrate();

        when(beverageService.getCrateById(mockCrate.getId())).thenReturn(mockCrate);
        when(beverageService.getBottles()).thenReturn(TestHelper.getMockBottles());

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/crate/edit/" + mockCrate.getId(), TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("crateId", mockCrate.getId()))
                .andExpect(MockMvcResultMatchers.model().attribute("bottleDropdownListItems", TestHelper.getMockBottleDropdownList()))
                .andExpect(MockMvcResultMatchers.model().attribute("crateUpdateDTO", new CrateUpdateDTO(mockCrate.getName(), mockCrate.getPicUrl(), mockCrate.getPrice(), mockCrate.getNoOfBottles(), mockCrate.getBottle().getId())))
                .andExpect(view().name("beverage/crate/edit"));
    }

    @Test
    public void getEditCrate_crateNotFound() throws Exception {
        long nonExistingCrateId = 0;

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/crate/edit/" + nonExistingCrateId, TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("hasServerError", true))
                .andExpect(MockMvcResultMatchers.model().attribute("crateId", nonExistingCrateId))
                .andExpect(MockMvcResultMatchers.model().attribute("crateUpdateDTO", new CrateUpdateDTO()))
                .andExpect(view().name("beverage/crate/edit"));
    }

    @Test
    public void getEditCrate_security() throws Exception {
        Crate crate = TestHelper.getCrate();

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/crate/edit/" + crate.getId(), null, new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/crate/edit/" + crate.getId(), TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @Transactional
    public void updateCrate_success() throws Exception {
        Crate crate = TestHelper.getCrate();

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/crate/edit/" + crate.getId(), TestHelper.getManager(), getCrateDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("/**/beverage/crate"));

        Optional<Crate> optionalAddedCrate = crateRepository.findById(crate.getId());

        assertTrue(optionalAddedCrate.isPresent());

        assertEquals(CRATE_NAME, optionalAddedCrate.get().getName());
        assertEquals(CRATE_PRICE, optionalAddedCrate.get().getPrice());
        assertEquals(CRATE_PIC_URL, optionalAddedCrate.get().getPicUrl());
        assertEquals(CRATE_IN_STOCK, optionalAddedCrate.get().getInStock());
        assertEquals(CRATE_NO_OF_BOTTLES, optionalAddedCrate.get().getNoOfBottles());
        assertEquals(TestHelper.getBottle().getId(), optionalAddedCrate.get().getBottle().getId());
    }

    @Test
    public void updateCrate_bottleNotFound() throws Exception {
        Crate crate = TestHelper.getCrate();

        MultiValueMap<String, String> params = getCrateDTOValidParams();
        params.put("bottleId", Collections.singletonList(String.valueOf(100)));

        when(beverageService.getBottles()).thenReturn(TestHelper.getMockBottles());

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/crate/edit/" + crate.getId(), TestHelper.getManager(), params))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("crateId", crate.getId()))
                .andExpect(MockMvcResultMatchers.model().attribute("hasServerError", true))
                .andExpect(MockMvcResultMatchers.model().attribute("bottleDropdownListItems", TestHelper.getMockBottleDropdownList()))
                .andExpect(view().name("beverage/crate/edit"));
    }

    @Test
    public void updateCrate_invalidData() throws Exception {
        Crate crate = TestHelper.getCrate();

        when(beverageService.getBottles()).thenReturn(TestHelper.getMockBottles());

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/crate/edit/" + crate.getId(), TestHelper.getManager(), getCrateDTOInvalidParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().hasErrors())
                .andExpect(MockMvcResultMatchers.model().attribute("crateId", crate.getId()))
                .andExpect(MockMvcResultMatchers.model().attribute("bottleDropdownListItems", TestHelper.getMockBottleDropdownList()))
                .andExpect(view().name("beverage/crate/edit"));
    }

    @Test
    public void updateCrate_crateNotFound() throws Exception {
        long nonExistingCrateId = 0;

        when(beverageService.getBottles()).thenReturn(TestHelper.getMockBottles());

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/crate/edit/" + nonExistingCrateId, TestHelper.getManager(), getCrateDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("hasServerError", true))
                .andExpect(MockMvcResultMatchers.model().attribute("crateId", nonExistingCrateId))
                .andExpect(MockMvcResultMatchers.model().attribute("bottleDropdownListItems", TestHelper.getMockBottleDropdownList()))
                .andExpect(view().name("beverage/crate/edit"));
    }

    @Test
    public void updateCrate_security() throws Exception {
        Crate crate = TestHelper.getCrate();

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/crate/edit/" + crate.getId(), null, getCrateDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/crate/edit/" + crate.getId(), TestHelper.getCustomer(), getCrateDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    private MultiValueMap<String, String> getCrateDTOValidParams() {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", CRATE_NAME);
        params.add("picUrl", CRATE_PIC_URL);
        params.add("price", String.valueOf(CRATE_PRICE));
        params.add("inStock", String.valueOf(CRATE_IN_STOCK));
        params.add("noOfBottles", String.valueOf(CRATE_NO_OF_BOTTLES));
        params.add("bottleId", String.valueOf(TestHelper.getBottle().getId()));

        return params;
    }

    private MultiValueMap<String, String> getCrateDTOInvalidParams() {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", null);
        params.add("picUrl", null);
        params.add("price", String.valueOf(0.0));
        params.add("inStock", String.valueOf(-1));
        params.add("noOfBottles", String.valueOf(0));
        params.add("bottleId", String.valueOf(0));

        return params;
    }

    private MultiValueMap<String, String> getBottleDTOValidParams() {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", BOTTLE_NAME);
        params.add("picUrl", BOTTLE_PIC_URL);
        params.add("supplier", BOTTLE_SUPPLIER);
        params.add("price", String.valueOf(BOTTLE_PRICE));
        params.add("volume", String.valueOf(BOTTLE_VOLUME));
        params.add("inStock", String.valueOf(BOTTLE_IN_STOCK));
        params.add("volumePercent", String.valueOf(BOTTLE_VOLUME_PERCENT));

        return params;
    }

    private MultiValueMap<String, String> getBottleDTOInvalidParams() {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", null);
        params.add("picUrl", null);
        params.add("supplier", null);
        params.add("price", String.valueOf(0.0));
        params.add("inStock", String.valueOf(-1));
        params.add("volume", String.valueOf(-1.0));
        params.add("volumePercent", String.valueOf(-1.0));

        return params;
    }
}
