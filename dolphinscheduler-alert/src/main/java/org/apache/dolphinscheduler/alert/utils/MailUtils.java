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

import org.apache.dolphinscheduler.common.enums.ShowType;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.util.*;

import static org.apache.dolphinscheduler.alert.utils.PropertyUtils.getInt;


/**
 * mail utils
 */
public class MailUtils {

    public static final Logger logger = LoggerFactory.getLogger(MailUtils.class);

    public static final String mailProtocol = PropertyUtils.getString(Constants.MAIL_PROTOCOL);

    public static final String mailServerHost = PropertyUtils.getString(Constants.MAIL_SERVER_HOST);

    public static final Integer mailServerPort = PropertyUtils.getInt(Constants.MAIL_SERVER_PORT);

    public static final String mailSender = PropertyUtils.getString(Constants.MAIL_SENDER);

    public static final String mailUser = PropertyUtils.getString(Constants.MAIL_USER);

    public static final String mailPasswd = PropertyUtils.getString(Constants.MAIL_PASSWD);

    public static final Boolean mailUseStartTLS = PropertyUtils.getBoolean(Constants.MAIL_SMTP_STARTTLS_ENABLE);

    public static final Boolean mailUseSSL = PropertyUtils.getBoolean(Constants.MAIL_SMTP_SSL_ENABLE);

    public static final String xlsFilePath = PropertyUtils.getString(Constants.XLS_FILE_PATH);

    public static final String starttlsEnable = PropertyUtils.getString(Constants.MAIL_SMTP_STARTTLS_ENABLE);

    public static final String sslEnable = PropertyUtils.getString(Constants.MAIL_SMTP_SSL_ENABLE);

    public static final String sslTrust = PropertyUtils.getString(Constants.MAIL_SMTP_SSL_TRUST);

    private static Template MAIL_TEMPLATE;

    static {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
        cfg.setDefaultEncoding(Constants.UTF_8);
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        cfg.setTemplateLoader(stringTemplateLoader);
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new FileInputStream(ResourceUtils.getFile(Constants.CLASSPATH_MAIL_TEMPLATES_ALERT_MAIL_TEMPLATE_FTL)),
                    Constants.UTF_8);

            MAIL_TEMPLATE = new Template("alert_mail_template", isr, cfg);
        } catch (Exception e) {
            MAIL_TEMPLATE = null;
            logger.error(e.getMessage(),e);
        } finally {
            IOUtils.closeQuietly(isr);
        }
    }


    /**
     * send mail to receivers
     * @param receivers the receiver list
     * @param title the title
     * @param content the content
     * @param showType the show type
     * @return the result map
     */
    public static Map<String,Object> sendMails(Collection<String> receivers, String title, String content,ShowType showType) {
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
    public static Map<String,Object> sendMails(Collection<String> receivers, Collection<String> receiversCc, String title, String content, ShowType showType) {
        Map<String,Object> retMap = new HashMap<>();
        retMap.put(Constants.STATUS, false);
        
        // if there is no receivers && no receiversCc, no need to process
        if (CollectionUtils.isEmpty(receivers) && CollectionUtils.isEmpty(receiversCc)) {
            return retMap;
        }

        receivers.removeIf((from) -> (StringUtils.isEmpty(from)));
        
        if (showType == ShowType.TABLE || showType == ShowType.TEXT){
            // send email
            HtmlEmail email = new HtmlEmail();

            try {
                Session session = getSession();
                email.setMailSession(session);
                email.setFrom(mailSender);
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
        }else if (showType == ShowType.ATTACHMENT || showType == ShowType.TABLEATTACHMENT){
            try {

                String partContent = (showType == ShowType.ATTACHMENT ? "Please see the attachment " + title + Constants.EXCEL_SUFFIX_XLS : htmlTable(content,false));

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
        if (StringUtils.isNotEmpty(content)){
            List<LinkedHashMap> mapItemsList = JSONUtils.toList(content, LinkedHashMap.class);

            if(!showAll && mapItemsList.size() > Constants.NUMBER_1000){
                mapItemsList = mapItemsList.subList(0,Constants.NUMBER_1000);
            }

            StringBuilder contents = new StringBuilder(200);

            boolean flag = true;

            String title = "";
            for (LinkedHashMap mapItems : mapItemsList){

                Set<Map.Entry<String, Object>> entries = mapItems.entrySet();

                Iterator<Map.Entry<String, Object>> iterator = entries.iterator();

                StringBuilder t = new StringBuilder(Constants.TR);
                StringBuilder cs = new StringBuilder(Constants.TR);
                while (iterator.hasNext()){

                    Map.Entry<String, Object> entry = iterator.next();
                    t.append(Constants.TH).append(entry.getKey()).append(Constants.TH_END);
                    cs.append(Constants.TD).append(String.valueOf(entry.getValue())).append(Constants.TD_END);

                }
                t.append(Constants.TR_END);
                cs.append(Constants.TR_END);
                if (flag){
                    title = t.toString();
                }
                flag = false;
                contents.append(cs);
            }

            return getTemplateContent(title,contents.toString());
        }

        return null;
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

        if (StringUtils.isNotEmpty(content)){
            List<String> list;
            try {
                list = JSONUtils.toList(content,String.class);
            }catch (Exception e){
                logger.error("json format exception",e);
                return null;
            }

            StringBuilder contents = new StringBuilder(100);
            for (String str : list){
                contents.append(Constants.TR);
                contents.append(Constants.TD).append(str).append(Constants.TD_END);
                contents.append(Constants.TR_END);
            }

            return getTemplateContent(null,contents.toString());

        }

        return null;
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
     * @param receivers
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
        msg.setFrom(new InternetAddress(mailSender));
        // 4. set receivers
        for (String receiver : receivers) {
            msg.addRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(receiver));
        }
        return msg;
    }

    /**
     * get session
     * @return the new Session
     */
    private static Session getSession() {
        Properties props = new Properties();
        props.setProperty(Constants.MAIL_HOST, mailServerHost);
        props.setProperty(Constants.MAIL_PORT, String.valueOf(mailServerPort));
        props.setProperty(Constants.MAIL_SMTP_AUTH, Constants.STRING_TRUE);
        props.setProperty(Constants.MAIL_TRANSPORT_PROTOCOL, mailProtocol);
        props.setProperty(Constants.MAIL_SMTP_STARTTLS_ENABLE, starttlsEnable);
        props.setProperty(Constants.MAIL_SMTP_SSL_ENABLE, sslEnable);
        props.setProperty(Constants.MAIL_SMTP_SSL_TRUST, sslTrust);

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // mail username and password
                return new PasswordAuthentication(mailUser, mailPasswd);
            }
        };

        Session session = Session.getInstance(props, auth);
        return session;
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
                msg.addRecipients(MimeMessage.RecipientType.CC, InternetAddress.parse(receiverCc));
            }
        }

        // set receivers type to cc
        // msg.addRecipients(MimeMessage.RecipientType.CC, InternetAddress.parse(propMap.get("${CC}")));
        // set subject
        msg.setSubject(title);
        MimeMultipart partList = new MimeMultipart();
        // set signature
        MimeBodyPart part1 = new MimeBodyPart();
        part1.setContent(partContent, Constants.TEXT_HTML_CHARSET_UTF_8);
        // set attach file
        MimeBodyPart part2 = new MimeBodyPart();
        // make excel file
        ExcelUtils.genExcelFile(content,title,xlsFilePath);
        File file = new File(xlsFilePath + Constants.SINGLE_SLASH +  title + Constants.EXCEL_SUFFIX_XLS);
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
    private static Map<String, Object> getStringObjectMap(String title, String content, ShowType showType, Map<String, Object> retMap, HtmlEmail email) throws EmailException {

        /**
         * the subject of the message to be sent
         */
        email.setSubject(title);
        /**
         * to send information, you can use HTML tags in mail content because of the use of HtmlEmail
         */
        if (showType == ShowType.TABLE) {
            email.setMsg(htmlTable(content));
        } else if (showType == ShowType.TEXT) {
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
                logger.info("delete success:"+file.getAbsolutePath()+file.getName());
            }else{
                logger.info("delete fail"+file.getAbsolutePath()+file.getName());
            }
        }else{
            logger.info("file not exists:"+file.getAbsolutePath()+file.getName());
        }
    }


    /**
     * handle exception
     * @param receivers the receiver list
     * @param retMap the result map
     * @param e the exception
     */
    private static void handleException(Collection<String> receivers, Map<String, Object> retMap, Exception e) {
        logger.error("Send email to {} failed", StringUtils.join(",", receivers), e);
        retMap.put(Constants.MESSAGE, "Send email to {" + StringUtils.join(",", receivers) + "} failed，" + e.toString());
    }

    /**
     * get the content of the template
     * @param title the title
     * @param content the content to retrieve
     * @return the content in the template or null if exception occurs
     */
    private static String getTemplateContent(String title,String content){
        StringWriter out = new StringWriter();
        Map<String,String> map = new HashMap<>();
        if(null != title){
            map.put(Constants.TITLE,title);
        }
        map.put(Constants.CONTENT,content);
        try {
            MAIL_TEMPLATE.process(map, out);
            return out.toString();
        } catch (TemplateException e) {
            logger.error(e.getMessage(),e);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }

        return null;
    }
}
