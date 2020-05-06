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
package org.apache.dolphinscheduler.alert.utils;

import org.apache.dolphinscheduler.alert.template.AlertTemplate;
import org.apache.dolphinscheduler.alert.template.AlertTemplateFactory;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.util.*;


/**
 * mail utils
 */
public class MailUtils {

    public static final Logger logger = LoggerFactory.getLogger(MailUtils.class);

    public static final String MAIL_PROTOCOL = PropertyUtils.getString(Constants.MAIL_PROTOCOL);

    public static final String MAIL_SERVER_HOST = PropertyUtils.getString(Constants.MAIL_SERVER_HOST);

    public static final Integer MAIL_SERVER_PORT = PropertyUtils.getInt(Constants.MAIL_SERVER_PORT);

    public static final String MAIL_SENDER = PropertyUtils.getString(Constants.MAIL_SENDER);

    public static final String MAIL_USER = PropertyUtils.getString(Constants.MAIL_USER);

    public static final String MAIL_PASSWD = PropertyUtils.getString(Constants.MAIL_PASSWD);

    public static final Boolean MAIL_USE_START_TLS = PropertyUtils.getBoolean(Constants.MAIL_SMTP_STARTTLS_ENABLE);

    public static final Boolean MAIL_USE_SSL = PropertyUtils.getBoolean(Constants.MAIL_SMTP_SSL_ENABLE);

    public static final String xlsFilePath = PropertyUtils.getString(Constants.XLS_FILE_PATH,"/tmp/xls");

    public static final String STARTTLS_ENABLE = PropertyUtils.getString(Constants.MAIL_SMTP_STARTTLS_ENABLE);

    public static final String SSL_ENABLE = PropertyUtils.getString(Constants.MAIL_SMTP_SSL_ENABLE);

    public static final String SSL_TRUST = PropertyUtils.getString(Constants.MAIL_SMTP_SSL_TRUST);

    public static final AlertTemplate alertTemplate = AlertTemplateFactory.getMessageTemplate();


    /**
     * send mail to receivers
     * @param receivers the receiver list
     * @param title the title
     * @param content the content
     * @param showType the show type
     * @return the result map
     */
    public static Map<String,Object> sendMails(Collection<String> receivers, String title, String content,String showType) {
        return sendMails(receivers, null, title, content, showType);
    }

    /**
     * send mail
     * @param receivers the receiver list
     * @param receiversCc cc list
     * @param title the title
     * @param content the content
     * @param showType the show type
     * @return the send result
     */
    public static Map<String,Object> sendMails(Collection<String> receivers, Collection<String> receiversCc, String title, String content, String showType) {
        Map<String,Object> retMap = new HashMap<>();
        retMap.put(Constants.STATUS, false);

        // if there is no receivers && no receiversCc, no need to process
        if (CollectionUtils.isEmpty(receivers) && CollectionUtils.isEmpty(receiversCc)) {
            return retMap;
        }

        receivers.removeIf(StringUtils::isEmpty);

        if (showType.equals(ShowType.TABLE.getDescp()) || showType.equals(ShowType.TEXT.getDescp())) {
            // send email
            HtmlEmail email = new HtmlEmail();

            try {
                Session session = getSession();
                email.setMailSession(session);
                email.setFrom(MAIL_SENDER);
                email.setCharset(Constants.UTF_8);
                if (CollectionUtils.isNotEmpty(receivers)){
                    // receivers mail
                    for (String receiver : receivers) {
                        email.addTo(receiver);
                    }
                }

                if (CollectionUtils.isNotEmpty(receiversCc)){
                    //cc
                    for (String receiverCc : receiversCc) {
                        email.addCc(receiverCc);
                    }
                }
                // sender mail
                return getStringObjectMap(title, content, showType, retMap, email);
            } catch (Exception e) {
                handleException(receivers, retMap, e);
            }
        }else if (showType.equals(ShowType.ATTACHMENT.getDescp()) || showType.equals(ShowType.TABLEATTACHMENT.getDescp())) {
            try {

                String partContent = (showType.equals(ShowType.ATTACHMENT.getDescp()) ? "Please see the attachment " + title + Constants.EXCEL_SUFFIX_XLS : htmlTable(content,false));

                attachment(receivers,receiversCc,title,content,partContent);

                retMap.put(Constants.STATUS, true);
                return retMap;
            }catch (Exception e){
                handleException(receivers, retMap, e);
                return retMap;
            }
        }
        return retMap;

    }

    /**
     * html table content
     * @param content the content
     * @param showAll if show the whole content
     * @return the html table form
     */
    private static String htmlTable(String content, boolean showAll){
        return alertTemplate.getMessageFromTemplate(content,ShowType.TABLE,showAll);
    }

    /**
     * html table content
     * @param content the content
     * @return the html table form
     */
    private static String htmlTable(String content){
        return htmlTable(content,true);
    }

    /**
     * html text content
     * @param content the content
     * @return text in html form
     */
    private static String htmlText(String content){
        return alertTemplate.getMessageFromTemplate(content,ShowType.TEXT);
    }

    /**
     * send mail as Excel attachment
     * @param receivers the receiver list
     * @param title the title
     * @throws Exception
     */
    private static void attachment(Collection<String> receivers,Collection<String> receiversCc,String title,String content,String partContent)throws Exception{
        MimeMessage msg = getMimeMessage(receivers);

        attachContent(receiversCc, title, content,partContent, msg);
    }

    /**
     * get MimeMessage
     * @param receivers receivers
     * @return the MimeMessage
     * @throws MessagingException
     */
    private static MimeMessage getMimeMessage(Collection<String> receivers) throws MessagingException {

        // 1. The first step in creating mail: creating session
        Session session = getSession();
        // Setting debug mode, can be turned off
        session.setDebug(false);

        // 2. creating mail: Creating a MimeMessage
        MimeMessage msg = new MimeMessage(session);
        // 3. set sender
        msg.setFrom(new InternetAddress(MAIL_SENDER));
        // 4. set receivers
        for (String receiver : receivers) {
            msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
        }
        return msg;
    }

    /**
     * get session
     * @return the new Session
     */
    private static Session getSession() {
        Properties props = new Properties();
        props.setProperty(Constants.MAIL_HOST, MAIL_SERVER_HOST);
        props.setProperty(Constants.MAIL_PORT, String.valueOf(MAIL_SERVER_PORT));
        props.setProperty(Constants.MAIL_SMTP_AUTH, Constants.STRING_TRUE);
        props.setProperty(Constants.MAIL_TRANSPORT_PROTOCOL, MAIL_PROTOCOL);
        props.setProperty(Constants.MAIL_SMTP_STARTTLS_ENABLE, STARTTLS_ENABLE);
        props.setProperty(Constants.MAIL_SMTP_SSL_ENABLE, SSL_ENABLE);
        props.setProperty(Constants.MAIL_SMTP_SSL_TRUST, SSL_TRUST);

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // mail username and password
                return new PasswordAuthentication(MAIL_USER, MAIL_PASSWD);
            }
        };

        return Session.getInstance(props, auth);
    }

    /**
     * attach content
     * @param receiversCc the cc list
     * @param title the title
     * @param content the content
     * @param partContent the partContent
     * @param msg the message
     * @throws MessagingException
     * @throws IOException
     */
    private static void attachContent(Collection<String> receiversCc, String title, String content, String partContent,MimeMessage msg) throws MessagingException, IOException {
        /**
         * set receiverCc
         */
        if(CollectionUtils.isNotEmpty(receiversCc)){
            for (String receiverCc : receiversCc){
                msg.addRecipients(Message.RecipientType.CC, InternetAddress.parse(receiverCc));
            }
        }

        // set subject
        msg.setSubject(title);
        MimeMultipart partList = new MimeMultipart();
        // set signature
        MimeBodyPart part1 = new MimeBodyPart();
        part1.setContent(partContent, Constants.TEXT_HTML_CHARSET_UTF_8);
        // set attach file
        MimeBodyPart part2 = new MimeBodyPart();
        File file = new File(xlsFilePath + Constants.SINGLE_SLASH +  title + Constants.EXCEL_SUFFIX_XLS);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        // make excel file

        ExcelUtils.genExcelFile(content,title,xlsFilePath);

        part2.attachFile(file);
        part2.setFileName(MimeUtility.encodeText(title + Constants.EXCEL_SUFFIX_XLS,Constants.UTF_8,"B"));
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
     * @param title the title
     * @param content the content
     * @param showType the showType
     * @param retMap the result map
     * @param email the email
     * @return the result map
     * @throws EmailException
     */
    private static Map<String, Object> getStringObjectMap(String title, String content, String showType, Map<String, Object> retMap, HtmlEmail email) throws EmailException {

        /**
         * the subject of the message to be sent
         */
        email.setSubject(title);
        /**
         * to send information, you can use HTML tags in mail content because of the use of HtmlEmail
         */
        if (showType.equals(ShowType.TABLE.getDescp())) {
            email.setMsg(htmlTable(content));
        } else if (showType.equals(ShowType.TEXT.getDescp())) {
            email.setMsg(htmlText(content));
        }

        // send
        email.send();

        retMap.put(Constants.STATUS, true);

        return retMap;
    }

    /**
     * file delete
     * @param file the file to delete
     */
    public static void deleteFile(File file){
        if(file.exists()){
            if(file.delete()){
                logger.info("delete success: {}",file.getAbsolutePath() + file.getName());
            }else{
                logger.info("delete fail: {}", file.getAbsolutePath() + file.getName());
            }
        }else{
            logger.info("file not exists: {}", file.getAbsolutePath() + file.getName());
        }
    }


    /**
     * handle exception
     * @param receivers the receiver list
     * @param retMap the result map
     * @param e the exception
     */
    private static void handleException(Collection<String> receivers, Map<String, Object> retMap, Exception e) {
        logger.error("Send email to {} failed", receivers, e);
        retMap.put(Constants.MESSAGE, "Send email to {" + String.join(",", receivers) + "} failed，" + e.toString());
    }

}
