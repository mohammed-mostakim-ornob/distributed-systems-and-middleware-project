package de.uniba.dsg.beverage_store.spring_boot.service;

import de.uniba.dsg.beverage_store.spring_boot.properties.InvoiceProperties;
import de.uniba.dsg.models.Invoice;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InvoiceService {
    private final InvoiceProperties invoiceProperties;

    public InvoiceService(InvoiceProperties invoiceProperties) {
        this.invoiceProperties = invoiceProperties;
    }

    public void generateInvoice(Invoice invoice) {
        new RestTemplate()
                .postForObject(invoiceProperties.getPdfGeneratorEndpoint(), invoice, String.class);
    }
}
