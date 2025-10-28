package com.spacex.laucncher.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.spacex.launcher.controller.AuthController;
import com.spacex.launcher.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_should_return_jwt_token() throws Exception {
        String email = "admin@example.com";
        String password = "admin123";

        Authentication authResult = new UsernamePasswordAuthenticationToken(email, null, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authResult);

        UserDetails ud = User.withUsername("admin").password("encoded-password").roles("ADMIN").build();
        when(userDetailsService.loadUserByUsername(email)).thenReturn(ud);

        when(jwtUtil.generateToken(ud)).thenReturn("test-jwt-token");

        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"));

        // Vérifier que AuthenticationManager a été appelé avec le bon
        // principal/credentials
        verify(authenticationManager).authenticate(argThat(arg -> {
            if (!(arg instanceof UsernamePasswordAuthenticationToken))
                return false;
            UsernamePasswordAuthenticationToken t = (UsernamePasswordAuthenticationToken) arg;
            return email.equals(t.getPrincipal()) && password.equals(t.getCredentials());
        }));
    }
}
