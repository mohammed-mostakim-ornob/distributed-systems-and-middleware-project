package de.uniba.dsg.beverage_store.spring_boot.api.controller;

import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.helper.Helper;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Crate;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.BeverageStockAddDTO;
import de.uniba.dsg.beverage_store.spring_boot.service.BeverageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(value = "/api/crates")
public class CrateRestController {

    private final BeverageService beverageService;

    @Autowired
    public CrateRestController(BeverageService beverageService) {
        this.beverageService = beverageService;
    }

    @PatchMapping(value = "/{id}/stock", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addToStock(@PathVariable("id") Long id, @RequestBody @Valid BeverageStockAddDTO request, Errors errors) {
        log.info("Adding stock to the Crate with ID: " + id + " - start");

        if (errors.hasErrors()) {
            log.info("Adding stock to the Crate with ID: " + id + " - failed, found model error");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Helper.constructErrorMessage(errors.getAllErrors()));
        }

        try {
            Crate crate = beverageService.addStockToCrate(id, request.getQuantity());

            log.info("Adding stock to the Crate with ID: " + id + " - completed");

            return ResponseEntity.status(HttpStatus.OK)
                    .body(crate);
        } catch (NotFoundException e) {
            log.info("Adding stock to the Crate with ID: " + id + " - failed, found not found exception");

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
