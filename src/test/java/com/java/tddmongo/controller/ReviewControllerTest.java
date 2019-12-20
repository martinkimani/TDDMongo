/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.tddmongo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.tddmongo.model.Review;
import com.java.tddmongo.model.ReviewEntry;
import com.java.tddmongo.service.ReviewService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author martin
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ReviewControllerTest {
    
    @MockBean
    private ReviewService service;
    
    @Autowired
    private MockMvc mockMvc;
    
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    @BeforeAll
    static void beforeAll(){
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    @Test
    @DisplayName("Get /review/reviewId - Found")
    void testGetReviewByIdFound() throws Exception {
        Review mockReview = new Review("reviewId", 1, 1);
        Date now = new Date();
        mockReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
        doReturn(Optional.of(mockReview)).when(service).findById("reviewId");
        
        mockMvc.perform(get("/review/{id}", "reviewId"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/review/reviewId"))
                
                .andExpect(jsonPath("$.id", is("reviewId")))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.entries[0].username", is("test-user")))
                .andExpect(jsonPath("$.entries[0].review", is("Great product")))
                .andExpect(jsonPath("$.entries[0].date", is(df.format(now))));
    }
    
    @Test
    @DisplayName("Get /review/reviewId - Not Found")
    void testGetReviewByIdNotFound() throws Exception {
        doReturn(Optional.empty()).when(service).findById("reviewId");
        
        mockMvc.perform(get("/review/{id}", "reviewId"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Post /review - Success")
    void testCreateReview() throws Exception {
        Date now = new Date();
        Review postReview =  new Review(1);
        postReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
        
        Review mockReview = new Review("reviewId", 1, 1);
        mockReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
        doReturn(mockReview).when(service).save(any());
        
        mockMvc.perform(post("/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(mockReview)))
        
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/review/reviewId"))
                
                .andExpect(jsonPath("$.id", is("reviewId")))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.entries[0].username", is("test-user")))
                .andExpect(jsonPath("$.entries[0].review", is("Great product")))
                .andExpect(jsonPath("$.entries[0].date", is(df.format(now))));
    }
    
    @Test
    @DisplayName("Post /review/{productId}/entry - Success")
    void testAddEntryToReview() throws Exception {
        Date now = new Date();
        ReviewEntry reviewEntry = new ReviewEntry("test-user", now, "Great product");
        
        Review mockReview = new Review("1", 1, 1);
        Review returnedReview = new Review("1", 1, 2);
        returnedReview.getEntries().add(reviewEntry);
        
        doReturn(Optional.of(mockReview)).when(service).findByProductId(1);
        doReturn(returnedReview).when(service).save(any());
        
        mockMvc.perform(post("/review/{productId}/entry",1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(reviewEntry)))
        
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(header().string(HttpHeaders.ETAG, "\"2\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/review/1"))
                
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.entries[0].username", is("test-user")))
                .andExpect(jsonPath("$.entries[0].review", is("Great product")))
                .andExpect(jsonPath("$.entries[0].date", is(df.format(now))));
    }
    
    static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
