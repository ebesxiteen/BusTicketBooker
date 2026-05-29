package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.ticketbooker.Service.ServiceImp.OAuthUserService;

@ExtendWith(MockitoExtension.class)
class OAuthUserServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    void getUserInfoCallsGooglePeopleApiWithBearerToken() {
        OAuthUserService service = new OAuthUserService(restTemplate);
        String expectedUrl = "https://people.googleapis.com/v1/people/me?personFields=names,emailAddresses,photos,genders,birthdays,addresses,phoneNumbers";
        ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), entityCaptor.capture(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"resourceName\":\"people/1\"}"));

        String result = service.getUserInfo("access-token");

        assertEquals("{\"resourceName\":\"people/1\"}", result);
        assertEquals("Bearer access-token", entityCaptor.getValue().getHeaders().getFirst("Authorization"));
        verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(entityCaptor.getValue()), eq(String.class));
    }
}
