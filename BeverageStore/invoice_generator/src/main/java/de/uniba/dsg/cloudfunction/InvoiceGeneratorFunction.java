package de.uniba.dsg.cloudfunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import de.uniba.dsg.helper.GoogleCloudStorageHelper;
import de.uniba.dsg.models.Invoice;
import de.uniba.dsg.models.InvoiceItem;

import javax.naming.directory.InvalidAttributesException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class InvoiceGeneratorFunction implements HttpFunction {

    private final String BUCKET_NAME = System.getenv("BUCKET_NAME");

    private static final Logger logger = Logger.getLogger(InvoiceGeneratorFunction.class.getName());

    @Override
    public void service(HttpRequest request, HttpResponse response)
            throws Exception {

        if (!request.getMethod().equalsIgnoreCase("POST")) {
            response.setStatusCode(HttpURLConnection.HTTP_FORBIDDEN);
            response.getWriter().write("Only POST request is permitted.");

            return;
        }

        try {
            Invoice invoice = new ObjectMapper()
                    .readValue(request.getReader(), Invoice.class);

            validateOrder(invoice);

            Map<String, String> metadata = Map.ofEntries(
                    new AbstractMap.SimpleEntry<>("email", invoice.getCustomerEmailId()),
                    new AbstractMap.SimpleEntry<>("order_number", invoice.getOrderNumber()),
                    new AbstractMap.SimpleEntry<>("customer_name", invoice.getCustomerName())
            );

            InvoiceGenerator invoiceGenerator = new InvoiceGenerator(invoice, "invoice_template");

            GoogleCloudStorageHelper.createFile(BUCKET_NAME, invoice.getOrderNumber() +  ".pdf", invoiceGenerator.generate(), metadata);

            invoiceGenerator.dispose();


            response.getWriter().write("Invoice successfully generated");
            response.setStatusCode(HttpURLConnection.HTTP_OK);

            logger.info("Generated invoice for Order: " + invoice.getOrderNumber());
        } catch (Exception ex) {
            response.getWriter().write(ex.getMessage());
            response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);

            logger.info("Exception: " + ex.toString());
        }
    }

    private void validateOrder(Invoice invoice)
            throws InvalidAttributesException {

        List<String> errors = new ArrayList<>();

        errors.addAll(validate(invoice));
        errors.addAll(validate(invoice.getDeliveryAddress()));
        errors.addAll(validate(invoice.getBillingAddress()));

        for (InvoiceItem item: invoice.getItems()) {
            errors.addAll(validate(item));
        }

        if (errors.isEmpty())
            return;

        throw new InvalidAttributesException(String.join("\n", errors));
    }

    private <T> List<String> validate(T obj) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        return validator.validate(obj)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
    }
}
