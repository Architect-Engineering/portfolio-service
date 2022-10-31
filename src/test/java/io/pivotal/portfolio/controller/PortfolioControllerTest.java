package io.pivotal.portfolio.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.pivotal.portfolio.config.SecurityConfig;
import io.pivotal.portfolio.config.ServiceTestConfiguration;
import io.pivotal.portfolio.config.TestSecurityConfiguration;
import io.pivotal.portfolio.domain.Order;
import io.pivotal.portfolio.service.PortfolioService;

/**
 * Tests for the PortfolioController.
 *
 * @author David Ferreira Pinto
 */

@RunWith(SpringRunner.class)
@Import(TestSecurityConfiguration.class)
@WebMvcTest(controllers = PortfolioController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
public class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService service;

    @Before
    public void before() {
        HashMap<String, Object> headers = new HashMap<>();
        headers.put("header1", "value1");
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("sub", "user@user.com");
    }


    @Test
    public void getPortfolio() throws Exception {
        when(service.getPortfolio())
                .thenReturn(ServiceTestConfiguration.portfolio());

        mockMvc.perform(
                get("/portfolio")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$.userName").value(
                                ServiceTestConfiguration.USER_ID))
                .andExpect(jsonPath("$.holdings.*").value(hasSize(1)))
                .andDo(print());
    }

    @Test
    public void addOrder() throws Exception {
        //when(token.getName()).thenReturn("userId");

        //when(token.getToken()).thenReturn(jwt);
        ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);

        when(service.addOrder(orderArgumentCaptor.capture()))
                .thenReturn(ServiceTestConfiguration.order2());

        mockMvc.perform(
                post("/portfolio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                convertObjectToJson(ServiceTestConfiguration.order())))
                .andExpect(status().isCreated()).andDo(print());
        assertEquals("davpin", orderArgumentCaptor.getValue().getUserId());

    }

    private String convertObjectToJson(Object request) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper.writeValueAsString(request);
    }

}
