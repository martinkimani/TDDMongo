/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.tddmongo.service;

import com.java.tddmongo.model.Review;
import com.java.tddmongo.model.ReviewEntry;
import com.java.tddmongo.repository.ReviewRepository;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Assertions;

/**
 *
 * @author martin
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReviewServiceTest {
    
    @Autowired
    private ReviewService service;
    
    @MockBean
    private ReviewRepository repository;
    
    @Test
    @DisplayName("test findById success")
    void testFindByIdSuccess() throws Exception{
        Date now = new Date();
        Review mockReview =  new Review("1", 1, 1);
        mockReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
        doReturn(Optional.of(mockReview)).when(repository).findById("1");
        
        Optional<Review> returnedReview = service.findById("1");
        
        Assertions.assertTrue(returnedReview.isPresent(), "Review should be found");
        Assertions.assertSame(mockReview, returnedReview.get(), "Review Object returned is not same");
    }
   
    @Test
    @DisplayName("test findById Not Found")
    void testFindByIdNotFound() throws Exception{
        Date now = new Date();
        Review mockReview =  new Review("1", 1, 1);
        mockReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
        doReturn(Optional.empty()).when(repository).findById("1");
        
        Optional<Review> returnedReview = service.findById("1");
        
        Assertions.assertFalse(returnedReview.isPresent(), "Review should not be found");
    }
    
    @Test
    @DisplayName("test findAll")
    void testFindAll() throws Exception{
        Date now = new Date();
        Review mockReview =  new Review("1", 1, 1);
        mockReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
        
        Review mockReview2 =  new Review("2", 1, 2);
        mockReview2.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
        doReturn(Arrays.asList(new Review[]{mockReview,mockReview2})).when(repository).findAll();
        
        List<Review> returnedReviews = service.findAll();
        
        Assertions.assertEquals(2, returnedReviews.size(), "should return 2 reviews");
    }
    
    @Test
    @DisplayName("test save review success")
    void testSaveReviewSuccess() throws Exception{
        Date now = new Date();
        Review saveReview =  new Review(1);
        saveReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
        
        Review mockReview =  new Review(1);
        mockReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
        
        doReturn(mockReview).when(repository).save(any());
        
        Review returnedReviews = service.save(saveReview);
        
        Assertions.assertNotNull(returnedReviews, "review object should not be null");
    }
}
