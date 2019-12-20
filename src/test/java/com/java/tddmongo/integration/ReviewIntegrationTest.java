/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.tddmongo.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.tddmongo.model.Review;
import com.java.tddmongo.model.ReviewEntry;
import com.java.tddmongo.repository.MongoDataFile;
import com.java.tddmongo.repository.MongoSpringExtension;
import java.util.Date;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author martin
 */
@ExtendWith({SpringExtension.class, MongoSpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
public class ReviewIntegrationTest {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private MockMvc mockMvc;
    
    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
    
    @Test
    @DisplayName("Get /review/1 - Found")
    @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
    void testGetReviewByIdFound() throws Exception {
        
        mockMvc.perform(get("/review/{id}", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/review/1"))
                
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.entries[0].username", is("user1")))
                .andExpect(jsonPath("$.entries[0].review", is("This is a review")))
                .andExpect(jsonPath("$.entries[0].date", is("2018-11-10T11:38:26.855+0000")));
    }
    
    @Test
    @DisplayName("Get /review/50 - Not Found")
    @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
    void testGetReviewByIdNotFound() throws Exception {
        
        mockMvc.perform(get("/review/{id}", "50"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Post /review - Success")
    @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
    void testCreateReview() throws Exception {
        Date now = new Date();
        Review postReview =  new Review(1);
        postReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
        
        mockMvc.perform(post("/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(postReview)))
        
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().exists(HttpHeaders.LOCATION))
                
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.entries[0].username", is("test-user")))
                .andExpect(jsonPath("$.entries[0].review", is("Great product")))
                .andExpect(jsonPath("$.entries[0].date").value(notNullValue()));
    }
    
    @Test
    @DisplayName("Post /review/{productId}/entry - Success")
    @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
    void testAddEntryToReview() throws Exception {
        ReviewEntry reviewEntry = new ReviewEntry("test-user", "Great product");
        
        mockMvc.perform(post("/review/{productId}/entry",1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(reviewEntry)))
        
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/review/1"))
                
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.entries[1].username", is("test-user")))
                .andExpect(jsonPath("$.entries[1].review", is("Great product")))
                .andExpect(jsonPath("$.entries[1].date").value(notNullValue()));
    }

    
    static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
