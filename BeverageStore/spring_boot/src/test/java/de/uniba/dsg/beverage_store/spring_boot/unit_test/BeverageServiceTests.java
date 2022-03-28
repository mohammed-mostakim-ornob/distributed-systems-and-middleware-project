package de.uniba.dsg.beverage_store.spring_boot.unit_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.demo.DemoData;
import de.uniba.dsg.beverage_store.spring_boot.exception.InsufficientStockException;
import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.model.BeverageType;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Bottle;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Crate;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.BottleDTO;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.BottleUpdateDTO;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.CrateDTO;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.CrateUpdateDTO;
import de.uniba.dsg.beverage_store.spring_boot.repository.BottleRepository;
import de.uniba.dsg.beverage_store.spring_boot.repository.CrateRepository;
import de.uniba.dsg.beverage_store.spring_boot.service.BeverageService;
import de.uniba.dsg.beverage_store.spring_boot.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BeverageServiceTests {

    @Resource(name = "sessionScopedCartService")
    private CartService cartService;

    @Autowired
    private BeverageService beverageService;

    @Autowired
    private CrateRepository crateRepository;

    @Autowired
    private BottleRepository bottleRepository;

    @Test
    public void getBottleById_success() throws NotFoundException {
        Bottle expectedBottle = TestHelper.getBottle();

        assertNotNull(expectedBottle);

        Bottle actualBottle = beverageService.getBottleById(expectedBottle.getId());

        assertEquals(expectedBottle.getId(), actualBottle.getId());
        assertEquals(expectedBottle.getName(), actualBottle.getName());
    }

    @Test
    public void getBottleById_bottleNotFound() {
        assertThrows(NotFoundException.class, () -> beverageService.getBottleById(0L));
    }

    @Test
    public void getBottles_success() {
        assertEquals(bottleRepository.findAll().size(), beverageService.getBottles().size());
    }

    @Test
    public void getPagedBottlesWithAllowedStock_success() throws NotFoundException, InsufficientStockException {
        for (int i = 1; i <= 3; i++) {
            Page<Bottle> bottles = beverageService.getPagedBottlesWithAllowedStock(i, 2);

            assertEquals((i < 3 ? 2 : 0), bottles.stream().count());

            for (Bottle bottle : bottles.getContent()) {
                assertEquals(bottle.getAllowedInStock(), bottle.getInStock());
            }
        }

        int addedQuantity = 2;
        Bottle bottle = TestHelper.getBottle();
        assertNotNull(bottle);

        cartService.addCartItem(BeverageType.BOTTLE, bottle.getId(), addedQuantity);

        Page<Bottle> bottles = beverageService.getPagedBottlesWithAllowedStock(1, DemoData.bottles.size());

        Bottle testBottle = bottles.stream()
                .filter(x -> x.getId().equals(bottle.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(testBottle);

        assertEquals(bottle.getInStock() - addedQuantity, testBottle.getAllowedInStock());
    }

    @Test
    @Transactional
    public void addBottle_success() {
        long countBeforeAdd = bottleRepository.count();

        BottleDTO bottleDTO = new BottleDTO(
                "Test Bottle",
                "https://www.google.com/logos/doodles/2020/december-holidays-day-1-6753651837108829.4-law.gif",
                1.0,
                10,
                1.0,
                0.0,
                "Test Supplier");

        Bottle addedBottle = beverageService.addBottle(bottleDTO);

        assertNotNull(addedBottle);
        assertNotNull(addedBottle.getId());
        assertEquals(bottleDTO.getName(), addedBottle.getName());
        assertEquals(countBeforeAdd + 1, bottleRepository.count());
    }

    @Test
    @Transactional
    public void updateBottle_success() throws NotFoundException {
        long countBeforeUpdate = bottleRepository.count();

        Bottle bottle = TestHelper.getBottle();

        assertNotNull(bottle);

        beverageService.updateBottle(bottle.getId(), new BottleUpdateDTO(
                "Test Bottle",
                "https://www.google.com/logos/doodles/2020/december-holidays-day-1-6753651837108829.4-law.gif",
                2.0,
                2.0,
                40.0,
                "Test Supplier"));

        Bottle updatedBottle = beverageService.getBottleById(bottle.getId());

        assertEquals(countBeforeUpdate, bottleRepository.count());

        assertEquals("Test Bottle", updatedBottle.getName());
        assertEquals(2.0, updatedBottle.getPrice());
        assertEquals(2.0, updatedBottle.getVolume());
        assertEquals(40.0, updatedBottle.getVolumePercent());
        assertEquals("Test Supplier", updatedBottle.getSupplier());
    }

    @Test
    public void updateBottle_bottleNotFound() {
        assertThrows(NotFoundException.class, () -> beverageService.updateBottle(0L, new BottleUpdateDTO(
                "Test Bottle 2",
                "https://www.google.com/logos/doodles/2020/december-holidays-day-1-6753651837108829.4-law.gif",
                2.0,
                2.0,
                40.0,
                "Test Supplier 2")));
    }

    @Test
    public void getCrateById_success() throws NotFoundException {
        Crate expectedCrate = TestHelper.getCrate();

        assertNotNull(expectedCrate);

        Crate actualCrate = beverageService.getCrateById(expectedCrate.getId());

        assertEquals(expectedCrate.getId(), actualCrate.getId());
        assertEquals(expectedCrate.getName(), actualCrate.getName());
    }

    @Test
    public void getCrateById_crateNotFound() {
        assertThrows(NotFoundException.class, () -> beverageService.getCrateById(0L));
    }

    @Test
    public void getPagedCratesWithAllowedStock_success() throws NotFoundException, InsufficientStockException {
        for (int i = 1; i <= 3; i++) {
            Page<Crate> crates = beverageService.getPagedCratesWithAllowedStock(i, 2);

            assertEquals((i < 3 ? 2 : 0), crates.stream().count());

            for (Crate crate : crates.getContent()) {
                assertEquals(crate.getAllowedInStock(), crate.getInStock());
            }
        }

        int addedQuantity = 2;
        Crate crate = TestHelper.getCrate();
        assertNotNull(crate);

        cartService.addCartItem(BeverageType.CRATE, crate.getId(), addedQuantity);

        Page<Crate> crates = beverageService.getPagedCratesWithAllowedStock(1, DemoData.crates.size());

        Crate testCrate = crates.stream()
                .filter(x -> x.getId().equals(crate.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(testCrate);

        assertEquals(crate.getInStock() - addedQuantity, testCrate.getAllowedInStock());
    }

    @Test
    @Transactional
    public void addCrate_success() throws NotFoundException {
        long countBeforeAdd = crateRepository.count();

        CrateDTO crateDTO = new CrateDTO(
                "Test Crate",
                "https://www.google.com/logos/doodles/2020/december-holidays-day-1-6753651837108829.4-law.gif",
                10.0,
                10,
                10,
                1L);

        Crate addedCrate = beverageService.addCrate(crateDTO);

        assertNotNull(addedCrate);
        assertNotNull(addedCrate.getId());
        assertEquals(crateDTO.getName(), addedCrate.getName());
        assertEquals(1L, addedCrate.getBottle().getId());
        assertEquals(countBeforeAdd + 1, crateRepository.count());
    }

    @Test
    public void addCrate_bottleNotFound() {
        assertThrows(NotFoundException.class, () -> beverageService.addCrate(new CrateDTO(
                "Test Crate",
                "https://www.google.com/logos/doodles/2020/december-holidays-day-1-6753651837108829.4-law.gif",
                10.0,
                10,
                10,
                0L)));
    }

    @Test
    @Transactional
    public void updateCrate_success() throws NotFoundException {
        long countBeforeUpdate = crateRepository.count();

        Crate crate = TestHelper.getCrate();

        assertNotNull(crate);

        beverageService.updateCrate(crate.getId(), new CrateUpdateDTO(
                "Test Crate",
                "https://www.google.com/logos/doodles/2020/december-holidays-day-1-6753651837108829.4-law.gif",
                20.0,
                20,
                2L));

        Crate updatedCrate = beverageService.getCrateById(crate.getId());

        assertEquals(countBeforeUpdate, crateRepository.count());

        assertEquals("Test Crate", updatedCrate.getName());
        assertEquals(20.0, updatedCrate.getPrice());
        assertEquals(20, updatedCrate.getNoOfBottles());
        assertEquals(2, updatedCrate.getBottle().getId());
    }

    @Test
    public void updateCrate_bottleNotFound() {
        Crate crate = TestHelper.getCrate();

        assertNotNull(crate);

        assertThrows(NotFoundException.class, () -> beverageService.updateCrate(crate.getId(), new CrateUpdateDTO(
                "Test Crate 2",
                "https://www.google.com/logos/doodles/2020/december-holidays-day-1-6753651837108829.4-law.gif",
                20.0,
                20,
                0L)));
    }

    @Test
    public void updateCrate_crateNotFound() {
        assertThrows(NotFoundException.class, () -> beverageService.updateCrate(0L, new CrateUpdateDTO(
                "Test Crate 2",
                "https://www.google.com/logos/doodles/2020/december-holidays-day-1-6753651837108829.4-law.gif",
                20.0,
                20,
                2L)));
    }

    @Test
    @Transactional
    public void addStockToBottle_success() throws NotFoundException {
        int addingStock = 50;
        long updateBottleId = 1L;

        int oldStock = beverageService.getBottleById(updateBottleId)
                .getInStock();

        beverageService.addStockToBottle(updateBottleId, addingStock);

        Bottle updatedBottle = beverageService.getBottleById(updateBottleId);

        assertEquals(oldStock + addingStock, updatedBottle.getInStock());
    }

    @Test
    public void addStockToBottle_bottleNotFound() {
        assertThrows(NotFoundException.class, () -> beverageService.addStockToBottle(0L, 50));
    }

    @Test
    @Transactional
    public void addStockToCrate_success() throws NotFoundException {
        int addingStock = 50;
        long updateCrateId = 1L;

        int oldStock = beverageService.getCrateById(updateCrateId)
                .getInStock();

        beverageService.addStockToCrate(updateCrateId, addingStock);

        Crate updatedCrate = beverageService.getCrateById(updateCrateId);

        assertEquals(oldStock + addingStock, updatedCrate.getInStock());
    }

    @Test
    public void addStockToCrate_crateNotFound() {
        assertThrows(NotFoundException.class, () -> beverageService.addStockToCrate(0L, 50));
    }
}
