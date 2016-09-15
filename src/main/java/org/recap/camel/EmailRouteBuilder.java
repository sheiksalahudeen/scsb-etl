package org.recap.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.FileUtils;
import org.recap.ReCAPConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by chenchulakshmig on 13/9/16.
 */
@Component
public class EmailRouteBuilder {

    private String emailBody;
    private String emailPassword;

    @Autowired
    public EmailRouteBuilder(CamelContext context, @Value("${data.dump.email.username}") String username, @Value("${data.dump.email.password.directory}") String passwordDirectory,
                             @Value("${data.dump.email.from}") String from, @Value("${data.dump.email.subject}") String subject,
                             @Value("${smtpServer}") String smtpServer) {
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    loadEmailBodyTemplateAndPassword();

                    from(ReCAPConstants.EMAIL_Q)
                            .routeId(ReCAPConstants.EMAIL_ROUTE_ID)
                            .setHeader("subject", simple(subject))
                            .setHeader("emailPayLoad").body(EmailPayLoad.class)
                            .setBody(simple(emailBody))
                            .setHeader("from", simple(from))
                            .setHeader("to", simple("${header.emailPayLoad.to}"))
                            .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                            .end();
                }

                private void loadEmailBodyTemplateAndPassword() {
                    URL resource = getClass().getResource("email_body.vm");
                    try {
                        emailBody = FileUtils.readFileToString(new File(resource.toURI()), "UTF-8");
                        File file = new File(passwordDirectory);
                        if (file.exists()) {
                            emailPassword = FileUtils.readFileToString(file, "UTF-8").trim();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
