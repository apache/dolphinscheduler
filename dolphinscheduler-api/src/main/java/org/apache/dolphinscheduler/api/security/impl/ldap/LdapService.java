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

package org.apache.dolphinscheduler.api.security.impl.ldap;

import org.apache.dolphinscheduler.api.security.LdapUserNotExistActionType;
import org.apache.dolphinscheduler.common.enums.UserType;

import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class LdapService {

    private static final Logger logger = LoggerFactory.getLogger(LdapService.class);

    @Value("${security.authentication.ldap.user.admin:#{null}}")
    private String adminUserId;

    @Value("${security.authentication.ldap.urls:#{null}}")
    private String ldapUrls;

    @Value("${security.authentication.ldap.base-dn:#{null}}")
    private String ldapBaseDn;

    @Value("${security.authentication.ldap.username:#{null}}")
    private String ldapSecurityPrincipal;

    @Value("${security.authentication.ldap.password:#{null}}")
    private String ldapPrincipalPassword;

    @Value("${security.authentication.ldap.user.identity-attribute:#{null}}")
    private String ldapUserIdentifyingAttribute;

    @Value("${security.authentication.ldap.user.email-attribute:#{null}}")
    private String ldapEmailAttribute;

    @Value("${security.authentication.ldap.user.not-exist-action:CREATE}")
    private String ldapUserNotExistAction;

    /***
     * get user type by configured admin userId
     * @param userId login userId
     * @return user type
     */
    public UserType getUserType(String userId) {
        return adminUserId.equalsIgnoreCase(userId) ? UserType.ADMIN_USER : UserType.GENERAL_USER;
    }

    /**
     * login by userId and return user email
     *
     * @param userId user identity id
     * @param userPwd user login password
     * @return user email
     */
    public String ldapLogin(String userId, String userPwd) {
        Properties searchEnv = getManagerLdapEnv();
        LdapContext ctx = null;
        try {
            // Connect to the LDAP server and Authenticate with a service user of whom we know the DN and credentials
            ctx = new InitialLdapContext(searchEnv, null);
            SearchControls sc = new SearchControls();
            sc.setReturningAttributes(new String[]{ldapEmailAttribute});
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            EqualsFilter filter = new EqualsFilter(ldapUserIdentifyingAttribute, userId);
            NamingEnumeration<SearchResult> results = ctx.search(ldapBaseDn, filter.toString(), sc);
            if (results.hasMore()) {
                // get the users DN (distinguishedName) from the result
                SearchResult result = results.next();
                NamingEnumeration<? extends Attribute> attrs = result.getAttributes().getAll();
                while (attrs.hasMore()) {
                    // Open another connection to the LDAP server with the found DN and the password
                    searchEnv.put(Context.SECURITY_PRINCIPAL, result.getNameInNamespace());
                    searchEnv.put(Context.SECURITY_CREDENTIALS, userPwd);
                    try {
                        new InitialDirContext(searchEnv);
                    } catch (Exception e) {
                        logger.warn("invalid ldap credentials or ldap search error", e);
                        return null;
                    }
                    Attribute attr = attrs.next();
                    if (attr.getID().equals(ldapEmailAttribute)) {
                        return (String) attr.get();
                    }
                }
            }
        } catch (NamingException e) {
            logger.error("ldap search error", e);
            return null;
        } finally {
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                logger.error("ldap context close error", e);
            }
        }

        return null;
    }

    /***
     * get ldap env fot ldap server search
     * @return Properties
     */
    Properties getManagerLdapEnv() {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ldapSecurityPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, ldapPrincipalPassword);
        env.put(Context.PROVIDER_URL, ldapUrls);
        return env;
    }

    public LdapUserNotExistActionType getLdapUserNotExistAction() {
        if (StringUtils.isBlank(ldapUserNotExistAction)) {
            logger.info(
                    "security.authentication.ldap.user.not.exist.action configuration is empty, the default value 'CREATE'");
            return LdapUserNotExistActionType.CREATE;
        }

        return LdapUserNotExistActionType.valueOf(ldapUserNotExistAction);
    }

    public boolean createIfUserNotExists() {
        return getLdapUserNotExistAction() == LdapUserNotExistActionType.CREATE;
    }
}
