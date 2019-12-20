/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.tddmongo.repository;

import com.java.tddmongo.model.Review;
import com.java.tddmongo.model.ReviewEntry;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 *
 * @author martin
 */
@DataMongoTest
@ExtendWith(MongoSpringExtension.class)
public class ReviewRepositoryTest {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private ReviewRepository repository;
    
    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
    
    @Test
    @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
    void testFindAll() throws Exception{
        List<Review> reviews = repository.findAll();
        Assertions.assertEquals(2, reviews.size(), "Should be two reviews found");
    }
    
    @Test
    @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
    void testFindByIdSuccess() throws Exception {
        Optional<Review> review = repository.findById("1");
        Assertions.assertTrue(review.isPresent(), "should return a review with id 1");
        review.ifPresent(r -> {
            Assertions.assertEquals("1", review.get().getId(), "review Id should be 1");
            Assertions.assertEquals(1, review.get().getProductId().intValue(), "product Id should be 1");
            Assertions.assertEquals(1, review.get().getVersion().intValue(), "version should be 1");
        });
    }
    
    @Test
    @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
    void testFindByIdFailed() throws Exception {
        Optional<Review> review = repository.findById("99");
        Assertions.assertFalse(review.isPresent(), "should not return a review with id 99");
    }
    
    @Test
    @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
    void testSaveReview() throws Exception {
        Date now = new Date();
        Review saveReview =  new Review(1);
        saveReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
        
        Review returnedreview = repository.save(saveReview);
        Assertions.assertNotNull(returnedreview, "should return a review object");
        
        Optional<Review> loadedReview = repository.findById(returnedreview.getId());
        Assertions.assertTrue(loadedReview.isPresent(), "should return a review with id "+returnedreview.getId());
    }
    
    @Test
    @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
    void testUpdateReview() throws Exception {
        Optional<Review> returnedreview = repository.findById("2");
        Assertions.assertTrue(returnedreview.isPresent(), "should return a review object with Id 2");
        Assertions.assertEquals(3, returnedreview.get().getEntries().size(), "review entries should be 3");
        
        Review updtReview = returnedreview.get();
        updtReview.getEntries().add(new ReviewEntry("new-user", "another great product"));
        repository.save(updtReview);
        
        Optional<Review> loadedReview = repository.findById("2");
        Assertions.assertTrue(loadedReview.isPresent(), "should return a review with id 2");
        Assertions.assertEquals(4, loadedReview.get().getEntries().size(), "review entries should be 4");
    }
    
    @Test
    @MongoDataFile(value = "sample6.json", classType = Review.class, collectionName = "Reviews")
    void testFindAll6() {
        List<Review> reviews = repository.findAll();
        Assertions.assertEquals(6, reviews.size(), "Should be six reviews in the database");
        reviews.forEach(System.out::println);
    }
}
