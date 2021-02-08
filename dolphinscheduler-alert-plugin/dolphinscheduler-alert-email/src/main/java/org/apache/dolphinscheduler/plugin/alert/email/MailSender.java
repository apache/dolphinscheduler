/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.alert.email;

import static java.util.Objects.requireNonNull;

import org.apache.dolphinscheduler.plugin.alert.email.exception.AlertEmailException;
import org.apache.dolphinscheduler.plugin.alert.email.template.AlertTemplate;
import org.apache.dolphinscheduler.plugin.alert.email.template.DefaultHTMLTemplate;
import org.apache.dolphinscheduler.spi.alert.AlertConstants;
import org.apache.dolphinscheduler.spi.alert.AlertResult;
import org.apache.dolphinscheduler.spi.alert.ShowType;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.smtp.SMTPProvider;

/**
 * mail utils
 */
public class MailSender {

    public static final Logger logger = LoggerFactory.getLogger(MailSender.class);

    private List<String> receivers;
    private List<String> receiverCcs;
    private String mailProtocol = "SMTP";
    private String mailSmtpHost;
    private String mailSmtpPort;
    private String mailSenderEmail;
    private String enableSmtpAuth;
    private String mailUser;
    private String mailPasswd;
    private String mailUseStartTLS;
    private String mailUseSSL;
    private String xlsFilePath;
    private String sslTrust;
    private String showType;
    private AlertTemplate alertTemplate;
    private String mustNotNull = "must not be null";

    public MailSender(Map<String, String> config) {

        String receiversConfig = config.get(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS);
        if (receiversConfig == null || "".equals(receiversConfig)) {
            throw new AlertEmailException(MailParamsConstants.PLUGIN_DEFAULT_EMAIL_RECEIVERS + mustNotNull);
        }

        receivers = Arrays.asList(receiversConfig.split(","));

        String receiverCcsConfig = config.get(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERCCS);

        receiverCcs = new ArrayList<>();
        if (receiverCcsConfig != null && !"".equals(receiverCcsConfig)) {
            receiverCcs = Arrays.asList(receiverCcsConfig.split(","));
        }

        mailSmtpHost = config.get(MailParamsConstants.NAME_MAIL_SMTP_HOST);
        requireNonNull(mailSmtpHost, MailParamsConstants.MAIL_SMTP_HOST + mustNotNull);

        mailSmtpPort = config.get(MailParamsConstants.NAME_MAIL_SMTP_PORT);
        requireNonNull(mailSmtpPort, MailParamsConstants.MAIL_SMTP_PORT + mustNotNull);

        mailSenderEmail = config.get(MailParamsConstants.NAME_MAIL_SENDER);
        requireNonNull(mailSenderEmail, MailParamsConstants.MAIL_SENDER + mustNotNull);

        enableSmtpAuth = config.get(MailParamsConstants.NAME_MAIL_SMTP_AUTH);

        mailUser = config.get(MailParamsConstants.NAME_MAIL_USER);
        requireNonNull(mailUser, MailParamsConstants.MAIL_USER + mustNotNull);

        mailPasswd = config.get(MailParamsConstants.NAME_MAIL_PASSWD);
        requireNonNull(mailPasswd, MailParamsConstants.MAIL_PASSWD + mustNotNull);

        mailUseStartTLS = config.get(MailParamsConstants.NAME_MAIL_SMTP_STARTTLS_ENABLE);
        requireNonNull(mailUseStartTLS, MailParamsConstants.MAIL_SMTP_STARTTLS_ENABLE + mustNotNull);

        mailUseSSL = config.get(MailParamsConstants.NAME_MAIL_SMTP_SSL_ENABLE);
        requireNonNull(mailUseSSL, MailParamsConstants.MAIL_SMTP_SSL_ENABLE + mustNotNull);

        sslTrust = config.get(MailParamsConstants.NAME_MAIL_SMTP_SSL_TRUST);
        requireNonNull(sslTrust, MailParamsConstants.MAIL_SMTP_SSL_TRUST + mustNotNull);

        showType = config.get(AlertConstants.SHOW_TYPE);
        requireNonNull(showType, AlertConstants.SHOW_TYPE + mustNotNull);

        xlsFilePath = config.get(EmailConstants.XLS_FILE_PATH);
        if (StringUtils.isBlank(xlsFilePath)) {
            xlsFilePath = "/tmp/xls";
        }

        alertTemplate = new DefaultHTMLTemplate();
    }

    /**
     * send mail to receivers
     *
     * @param title title
     * @param content content
     */
    public AlertResult sendMails(String title, String content) {
        return sendMails(this.receivers, this.receiverCcs, title, content);
    }

    /**
     * send mail to receivers
     *
     * @param title email title
     * @param content email content
     */
    public AlertResult sendMailsToReceiverOnly(String title, String content) {
        return sendMails(this.receivers, null, title, content);
    }

    /**
     * send mail
     *
     * @param receivers receivers
     * @param receiverCcs receiverCcs
     * @param title title
     * @param content content
     */
    public AlertResult sendMails(List<String> receivers, List<String> receiverCcs, String title, String content) {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus("false");

        // if there is no receivers && no receiversCc, no need to process
        if (CollectionUtils.isEmpty(receivers) && CollectionUtils.isEmpty(receiverCcs)) {
            return alertResult;
        }

        receivers.removeIf(StringUtils::isEmpty);

        if (showType.equals(ShowType.TABLE.getDescp()) || showType.equals(ShowType.TEXT.getDescp())) {
            // send email
            HtmlEmail email = new HtmlEmail();

            try {
                Session session = getSession();
                email.setMailSession(session);
                email.setFrom(mailSenderEmail);
                email.setCharset(EmailConstants.UTF_8);
                if (CollectionUtils.isNotEmpty(receivers)) {
                    // receivers mail
                    for (String receiver : receivers) {
                        email.addTo(receiver);
                    }
                }

                if (CollectionUtils.isNotEmpty(receiverCcs)) {
                    //cc
                    for (String receiverCc : receiverCcs) {
                        email.addCc(receiverCc);
                    }
                }
                // sender mail
                return getStringObjectMap(title, content, alertResult, email);
            } catch (Exception e) {
                handleException(alertResult, e);
            }
        } else if (showType.equals(ShowType.ATTACHMENT.getDescp()) || showType.equals(ShowType.TABLEATTACHMENT.getDescp())) {
            try {

                String partContent = (showType.equals(ShowType.ATTACHMENT.getDescp()) ? "Please see the attachment " + title + EmailConstants.EXCEL_SUFFIX_XLS : htmlTable(content, false));

                attachment(title, content, partContent);

                alertResult.setStatus("true");
                return alertResult;
            } catch (Exception e) {
                handleException(alertResult, e);
                return alertResult;
            }
        }
        return alertResult;

    }

    /**
     * html table content
     *
     * @param content the content
     * @param showAll if show the whole content
     * @return the html table form
     */
    private String htmlTable(String content, boolean showAll) {
        return alertTemplate.getMessageFromTemplate(content, ShowType.TABLE, showAll);
    }

    /**
     * html table content
     *
     * @param content the content
     * @return the html table form
     */
    private String htmlTable(String content) {
        return htmlTable(content, true);
    }

    /**
     * html text content
     *
     * @param content the content
     * @return text in html form
     */
    private String htmlText(String content) {
        return alertTemplate.getMessageFromTemplate(content, ShowType.TEXT);
    }

    /**
     * send mail as Excel attachment
     */
    private void attachment(String title, String content, String partContent) throws Exception {
        MimeMessage msg = getMimeMessage();

        attachContent(title, content, partContent, msg);
    }

    /**
     * get MimeMessage
     */
    private MimeMessage getMimeMessage() throws MessagingException {

        // 1. The first step in creating mail: creating session
        Session session = getSession();
        // Setting debug mode, can be turned off
        session.setDebug(false);

        // 2. creating mail: Creating a MimeMessage
        MimeMessage msg = new MimeMessage(session);
        // 3. set sender
        msg.setFrom(new InternetAddress(mailSenderEmail));
        // 4. set receivers
        for (String receiver : receivers) {
            msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
        }
        return msg;
    }

    /**
     * get session
     *
     * @return the new Session
     */
    private Session getSession() {
        Properties props = new Properties();
        props.setProperty(MailParamsConstants.MAIL_SMTP_HOST, mailSmtpHost);
        props.setProperty(MailParamsConstants.MAIL_SMTP_PORT, mailSmtpPort);
        props.setProperty(MailParamsConstants.MAIL_SMTP_AUTH, enableSmtpAuth);
        props.setProperty(EmailConstants.MAIL_TRANSPORT_PROTOCOL, mailProtocol);
        props.setProperty(MailParamsConstants.MAIL_SMTP_STARTTLS_ENABLE, mailUseStartTLS);
        props.setProperty(MailParamsConstants.MAIL_SMTP_SSL_ENABLE, mailUseSSL);
        props.setProperty(MailParamsConstants.MAIL_SMTP_SSL_TRUST, sslTrust);

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // mail username and password
                return new PasswordAuthentication(mailUser, mailPasswd);
            }
        };

        Session session = Session.getInstance(props, auth);
        session.addProvider(new SMTPProvider());
        return session;
    }

    /**
     * attach content
     */
    private void attachContent(String title, String content, String partContent, MimeMessage msg) throws MessagingException, IOException {
        /*
         * set receiverCc
         */
        if (CollectionUtils.isNotEmpty(receiverCcs)) {
            for (String receiverCc : receiverCcs) {
                msg.addRecipients(Message.RecipientType.CC, InternetAddress.parse(receiverCc));
            }
        }

        // set subject
        msg.setSubject(title);
        MimeMultipart partList = new MimeMultipart();
        // set signature
        MimeBodyPart part1 = new MimeBodyPart();
        part1.setContent(partContent, EmailConstants.TEXT_HTML_CHARSET_UTF_8);
        // set attach file
        MimeBodyPart part2 = new MimeBodyPart();
        File file = new File(xlsFilePath + EmailConstants.SINGLE_SLASH + title + EmailConstants.EXCEL_SUFFIX_XLS);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        // make excel file

        ExcelUtils.genExcelFile(content, title, xlsFilePath);

        part2.attachFile(file);
        part2.setFileName(MimeUtility.encodeText(title + EmailConstants.EXCEL_SUFFIX_XLS, EmailConstants.UTF_8, "B"));
        // add components to collection
        partList.addBodyPart(part1);
        partList.addBodyPart(part2);
        msg.setContent(partList);
        // 5. send Transport
        Transport.send(msg);
        // 6. delete saved file
        deleteFile(file);
    }

    /**
     * the string object map
     */
    private AlertResult getStringObjectMap(String title, String content, AlertResult alertResult, HtmlEmail email) throws EmailException {

        /*
         * the subject of the message to be sent
         */
        email.setSubject(title);
        /*
         * to send information, you can use HTML tags in mail content because of the use of HtmlEmail
         */
        if (showType.equals(ShowType.TABLE.getDescp())) {
            email.setMsg(htmlTable(content));
        } else if (showType.equals(ShowType.TEXT.getDescp())) {
            email.setMsg(htmlText(content));
        }

        // send
        email.setDebug(true);
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        email.send();

        alertResult.setStatus("true");

        return alertResult;
    }

    /**
     * file delete
     *
     * @param file the file to delete
     */
    public void deleteFile(File file) {
        if (file.exists()) {
            if (file.delete()) {
                logger.info("delete success: {}", file.getAbsolutePath() + file.getName());
            } else {
                logger.info("delete fail: {}", file.getAbsolutePath() + file.getName());
            }
        } else {
            logger.info("file not exists: {}", file.getAbsolutePath() + file.getName());
        }
    }

    /**
     * handle exception
     */
    private void handleException(AlertResult alertResult, Exception e) {
        logger.error("Send email to {} failed", receivers, e);
        alertResult.setMessage("Send email to {" + String.join(",", receivers) + "} failed，" + e.toString());
    }

}
