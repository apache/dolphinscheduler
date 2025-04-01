//package org.apache.dolphinscheduler.api.controller;
//
//
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//import org.apache.dolphinscheduler.api.security.impl.oidc.OIDCAuthenticator;
//import org.apache.dolphinscheduler.auth.oidc.OidcUser;
//import org.apache.dolphinscheduler.api.security.impl.oidc.OidcTokenValidator;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.Optional;
//
//public class OidcAuthenticatorTest {
//
//    @Mock
//    private OidcTokenValidator tokenValidator;
//
//    @InjectMocks
//    private OidcAuthenticator oidcAuthenticator;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testAuthenticateWithValidToken() {
//        String validToken = "valid.jwt.token";
//        OidcUser mockUser = new OidcUser("testUser", "test@example.com", "user");
//
//        when(tokenValidator.validateToken(validToken)).thenReturn(Optional.of(mockUser));
//
//        Optional<OidcUser> user = oidcAuthenticator.authenticate(validToken);
//        assertTrue(user.isPresent());
//        assertEquals("testUser", user.get().getUsername());
//    }
//
//    @Test
//    void testAuthenticateWithInvalidToken() {
//        String invalidToken = "invalid.jwt.token";
//        when(tokenValidator.validateToken(invalidToken)).thenReturn(Optional.empty());
//
//        Optional<OidcUser> user = oidcAuthenticator.authenticate(invalidToken);
//        assertFalse(user.isPresent());
//    }
//
//    @Test
//    void testAuthenticateWithExpiredToken() {
//        String expiredToken = "expired.jwt.token";
//        when(tokenValidator.validateToken(expiredToken)).thenReturn(Optional.empty());
//
//        Optional<OidcUser> user = oidcAuthenticator.authenticate(expiredToken);
//        assertFalse(user.isPresent());
//    }
//
//    @Test
//    void testAuthenticateWithTamperedToken() {
//        String tamperedToken = "tampered.jwt.token";
//        when(tokenValidator.validateToken(tamperedToken)).thenReturn(Optional.empty());
//
//        Optional<OidcUser> user = oidcAuthenticator.authenticate(tamperedToken);
//        assertFalse(user.isPresent());
//    }
//
//    @Test
//    void testAuthenticateWithMissingScope() {
//        String missingScopeToken = "missing.scope.jwt.token";
//        when(tokenValidator.validateToken(missingScopeToken)).thenReturn(Optional.empty());
//
//        Optional<OidcUser> user = oidcAuthenticator.authenticate(missingScopeToken);
//        assertFalse(user.isPresent());
//    }
//
//    @Test
//    void testAuthenticateWithValidAdminUser() {
//        String validToken = "admin.jwt.token";
//        OidcUser adminUser = new OidcUser("adminUser", "admin@example.com", "admin");
//
//        when(tokenValidator.validateToken(validToken)).thenReturn(Optional.of(adminUser));
//
//        Optional<OidcUser> user = oidcAuthenticator.authenticate(validToken);
//        assertTrue(user.isPresent());
//        assertEquals("adminUser", user.get().getUsername());
//        assertEquals("admin", user.get().getRole());
//    }
//}
//
