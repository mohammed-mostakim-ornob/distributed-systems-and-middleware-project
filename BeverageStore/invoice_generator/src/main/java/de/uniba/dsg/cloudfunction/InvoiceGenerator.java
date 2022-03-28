package de.uniba.dsg.cloudfunction;

import com.lowagie.text.DocumentException;
import de.uniba.dsg.models.Invoice;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InvoiceGenerator {
    private final Invoice invoice;
    private final String templateName;
    private final ByteArrayOutputStream outputStream;

    public InvoiceGenerator(Invoice invoice, String templateName) {
        this.invoice = invoice;
        this.templateName = templateName;
        outputStream = new ByteArrayOutputStream();
    }

    public byte[] generate() throws DocumentException {
        String html = parseThymeleafTemplate();

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);

        return outputStream.toByteArray();
    }

    public void dispose() throws IOException {
        outputStream.close();
    }

    private String parseThymeleafTemplate() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariable("invoice", invoice);

        return templateEngine.process(templateName, context);
    }
}
