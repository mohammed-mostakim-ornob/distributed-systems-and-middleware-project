package de.uniba.dsg.beverage_store.spring_boot.api.controller;

import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.helper.Helper;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrder;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrderItem;
import de.uniba.dsg.beverage_store.spring_boot.service.InvoiceService;
import de.uniba.dsg.beverage_store.spring_boot.service.OrderService;
import de.uniba.dsg.models.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/invoice")
public class InvoiceRestController {

    private final OrderService orderService;
    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceRestController(OrderService orderService,
                                 InvoiceService invoiceService) {
        this.orderService = orderService;
        this.invoiceService = invoiceService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> generateInvoice(@RequestBody @Valid Invoice invoice, Errors errors) {
        log.info("Generating invoice - start");

        if (errors.hasErrors()) {
            log.info("Generating invoice - failed, found model error");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Helper.constructErrorMessage(errors.getAllErrors()));
        }

        try {
            invoiceService.generateInvoice(invoice);

            log.info("Generating invoice - completed");

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Invoice successfully generated and sent to the Email - " + invoice.getCustomerEmailId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error.");
        }
    }

    @PostMapping(value = "/order/{order-number}")
    public ResponseEntity<?> generateOrderInvoice(@PathVariable("order-number") String orderNumber) {
        log.info("Generating invoice of the Order with with Order number: " + orderNumber + " - start");

        try {
            BeverageOrder order = orderService.getOrderByOrderNumber(orderNumber);
            List<BeverageOrderItem> orderItems = orderService.getOrderItemsByOrderNumber(orderNumber);

            Invoice invoice = Helper.constructOrderInvoice(order, order.getUser(), order.getDeliveryAddress(), order.getBillingAddress(), orderItems);

            invoiceService.generateInvoice(invoice);

            log.info("Generating invoice of the Order with with Order number: " + orderNumber + " - completed");

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Invoice successfully generated for Order: " + orderNumber);
        } catch (NotFoundException e) {
            log.info("Generating invoice of the Order with with Order number: " + orderNumber + " - failed, found not found exception");

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
