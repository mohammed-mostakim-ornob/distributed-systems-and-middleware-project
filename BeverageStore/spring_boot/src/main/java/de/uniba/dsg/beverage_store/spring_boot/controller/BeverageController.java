package de.uniba.dsg.beverage_store.spring_boot.controller;

import de.uniba.dsg.beverage_store.spring_boot.model.DropdownListItem;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Bottle;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Crate;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.*;
import de.uniba.dsg.beverage_store.spring_boot.properties.BottleProperties;
import de.uniba.dsg.beverage_store.spring_boot.properties.CrateProperties;
import de.uniba.dsg.beverage_store.spring_boot.service.BeverageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "/beverage")
public class BeverageController {

    private final BeverageService beverageService;

    private final CrateProperties crateProperties;
    private final BottleProperties bottleProperties;

    @Autowired
    public BeverageController(
            BeverageService beverageService,
            CrateProperties crateProperties,
            BottleProperties bottleProperties) {
        this.beverageService = beverageService;

        this.crateProperties = crateProperties;
        this.bottleProperties = bottleProperties;
    }

    @GetMapping(value = "/bottle")
    public String getBottles(@RequestParam(defaultValue = "1") int page, Model model) {
        log.info("Retrieving bottle page: " + page + " - start");

        Page<Bottle> bottlePage = beverageService.getPagedBottlesWithAllowedStock(page, bottleProperties.getPageSize());

        model.addAttribute("bottles", bottlePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("numberOfPages", bottlePage.getTotalPages());

        log.info("Retrieving bottle page: " + page + " - completed");

        return "beverage/bottle/list";
    }

    @GetMapping(value = "/bottle/add")
    public String getAddBottle(Model model) {
        model.addAttribute("bottleDTO", new BottleDTO());

        return "beverage/bottle/add";
    }

    @PostMapping(value = "/bottle/add")
    public String addBottle(@Valid BottleDTO bottleDTO, Errors errors, Model model) {
        log.info("Creating bottle - start");

        boolean hasModelError = false, hasServerError = false;

        if (errors.hasErrors()) {
            hasModelError = true;

            log.info("Creating bottle - failed, found model error");
        }

        if (!hasModelError) {
            try {
                beverageService.addBottle(bottleDTO);

                log.info("Creating bottle - completed");
            } catch (Exception e) {
                hasServerError = true;

                log.info("Creating bottle - failed, found server error");
            }
        }

        if (hasModelError || hasServerError) {
            model.addAttribute("hasServerError", hasServerError);

            return "beverage/bottle/add";
        }

        return "redirect:/beverage/bottle";
    }

    @GetMapping(value = "/bottle/edit/{id}")
    public String getEditBottle(@PathVariable("id") long id, Model model) {
        try {
            log.info("Retrieving bottle with ID: " + id + " - start");

            Bottle bottle = beverageService.getBottleById(id);

            model.addAttribute("bottleUpdateDTO", new BottleUpdateDTO(bottle.getName(), bottle.getPicUrl(), bottle.getPrice(), bottle.getVolume(), bottle.getVolumePercent(), bottle.getSupplier()));

            log.info("Retrieving bottle with ID: " + id + " - completed");
        } catch (Exception e) {
            model.addAttribute("bottleUpdateDTO", new BottleUpdateDTO());
            model.addAttribute("hasServerError", true);

            log.info("Retrieving bottle with ID: " + id + " - failed, server error found");
        }

        model.addAttribute("bottleId", id);

        return "beverage/bottle/edit";
    }

    @PostMapping(value = "/bottle/edit/{id}")
    public String updateBottle(@PathVariable("id") long id, @Valid BottleUpdateDTO bottleUpdateDTO, Errors errors, Model model) {
        log.info("Updating bottle - start");

        boolean hasModelError = false, hasServerError = false;

        if (errors.hasErrors()) {
            hasModelError = true;

            log.info("Updating bottle - failed, found model error");
        }

        if (!hasModelError) {
            try {
                beverageService.updateBottle(id, bottleUpdateDTO);

                log.info("Updating bottle - completed");
            } catch (Exception e) {
                hasServerError = true;

                log.info("Updating bottle - failed, found server error");
            }
        }

        if (hasModelError || hasServerError) {
            model.addAttribute("bottleId", id);
            model.addAttribute("hasServerError", hasServerError);

            return "beverage/bottle/edit";
        }

        return "redirect:/beverage/bottle";
    }

    @GetMapping(value = "/crate")
    public String getCrates(@RequestParam(defaultValue = "1") int page, Model model) {
        log.info("Retrieving crate page: " + page + " - start");

        Page<Crate> cratePage = beverageService.getPagedCratesWithAllowedStock(page, crateProperties.getPageSize());

        model.addAttribute("crates", cratePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("numberOfPages", cratePage.getTotalPages());

        log.info("Retrieving crate page: " + page + " - completed");

        return "beverage/crate/list";
    }

    @GetMapping(value = "/crate/add")
    public String getAddCrate(Model model) {
        model.addAttribute("crateDTO", new CrateDTO());
        model.addAttribute("bottleDropdownListItems", getBottleDropdownList());

        return "beverage/crate/add";
    }

    @PostMapping(value = "/crate/add")
    public String addCrate(@Valid CrateDTO crateDTO, Errors errors, Model model) {
        log.info("Creating crate - start");

        boolean hasModelError = false, hasServerError = false;

        if (errors.hasErrors()) {
            hasModelError = true;

            log.info("Creating crate - failed, found model error");
        }

        if (!hasModelError) {
            try {
                beverageService.addCrate(crateDTO);

                log.info("Creating crate - completed");
            } catch (Exception e) {
                hasServerError = true;

                log.info("Creating crate - failed, found server error");
            }
        }

        if (hasModelError || hasServerError) {
            model.addAttribute("hasServerError", hasServerError);
            model.addAttribute("bottleDropdownListItems", getBottleDropdownList());

            return "beverage/crate/add";
        }

        return "redirect:/beverage/crate";
    }

    @GetMapping(value = "/crate/edit/{id}")
    public String getEditCrate(@PathVariable("id") long id, Model model) {
        try {
            log.info("Retrieving crate with ID: " + id + " - start");

            Crate crate = beverageService.getCrateById(id);

            model.addAttribute("crateId", crate.getId());
            model.addAttribute("crateUpdateDTO", new CrateUpdateDTO(crate.getName(), crate.getPicUrl(), crate.getPrice(), crate.getNoOfBottles(), crate.getBottle().getId()));
            model.addAttribute("bottleDropdownListItems", getBottleDropdownList());

            log.info("Retrieving crate with ID: " + id + " - completed");
        } catch (Exception e) {
            model.addAttribute("crateId", id);
            model.addAttribute("crateUpdateDTO", new CrateUpdateDTO());
            model.addAttribute("hasServerError", true);
            model.addAttribute("bottleDropdownListItems", getBottleDropdownList());

            log.info("Retrieving crate with ID: " + id + " - failed, server error found");
        }

        return "beverage/crate/edit";
    }

    @PostMapping(value = "/crate/edit/{id}")
    public String updateCrate(@PathVariable("id") long id, @Valid CrateUpdateDTO crateUpdateDTO, Errors errors, Model model) {
        log.info("Updating crate - start");

        boolean hasModelError = false, hasServerError = false;

        if (errors.hasErrors()) {
            hasModelError = true;

            log.info("Updating crate - failed, found model error");
        }

        if (!hasModelError) {
            try {
                beverageService.updateCrate(id, crateUpdateDTO);

                log.info("Updating crate - completed");
            } catch (Exception e) {
                hasServerError = true;

                log.info("Updating crate - failed, found server error");
            }
        }

        if (hasModelError || hasServerError) {
            model.addAttribute("crateId", id);
            model.addAttribute("hasServerError", hasServerError);
            model.addAttribute("bottleDropdownListItems", getBottleDropdownList());

            return "beverage/crate/edit";
        }

        return "redirect:/beverage/crate";
    }

    private List<DropdownListItem<Long>> getBottleDropdownList() {
        return beverageService.getBottles()
                .stream()
                .map(Bottle::getDropdownListItem)
                .collect(Collectors.toList());
    }
}
