package com.example.demo.controllers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import com.example.demo.models.ApiResponse;
import com.example.demo.models.UserDto;
import com.example.demo.models.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2UserRequestEntityConverter;
import org.springframework.security.oauth2.core.user.OAuth2UserRequestFactory;
import org.springframework.security.oauth2.core.user.OAuth2UserRequestFactoryProvider;
import org.springframework.security.oauth2.core.user.OAuth2UserRequestUriBuilder;
import org.springframework.security.oauth2.core.user.OAuth2UserResponse;
import org.springframework.security.oauth2.core.user.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2UserToken;
import org.springframework.security.oauth2.core.user.OAuth2UserTokenRepository;
import org.springframework.security.oauth2.core.user.OAuth2UserTokenService;
import org.springframework.security.oauth2.core.user.OAuth2UserTokenValidator;
import org.springframework.security.oauth2.core.user.OAuth2UserTokenValidatorResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;


import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.models.ApiResponse;
import com.example.demo.models.UserDto;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

@RestController
@RequestMapping("/api")
public class app {

    @Autowired
    private FirebaseService firebaseService;

    @Service
    public class FirebaseService {
        private final FirebaseApp firebaseApp;

        @Autowired
        public FirebaseService() throws IOException {
            FileInputStream serviceAccount = new FileInputStream("path/to/your/serviceAccountKey.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://your-project-id.firebaseio.com")
                    .build();

            firebaseApp = FirebaseApp.initializeApp(options);
        }

        public FirebaseApp getFirebaseApp() {
            return firebaseApp;
        }
    }
    public static void main(String[] args) {
        
    }


  
    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {
        try {
            UserRecord userRecord = firebaseService.createUser(userDto.getEmail(), userDto.getPassword());
            return ResponseEntity.ok(new ApiResponse(true, "User created successfully"));
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Error creating user"));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDto userDto) {
        try {
            String token = firebaseService.loginUser(userDto.getEmail(), userDto.getPassword());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "Invalid email or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String token) {
        try {
            firebaseService.logoutUser(token);
            return ResponseEntity.ok(new ApiResponse(true, "User logged out successfully"));
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Error logging out user"));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String token) {
        try {
            UserProfileDto userProfileDto = firebaseService.getUserProfile(token);
            return ResponseEntity.ok(userProfileDto);
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Error getting user profile"));
        }
    }

    
    @PutMapping("/user")
    public ResponseEntity<?> updateUserProfile(@RequestHeader("Authorization") String token, @RequestBody UserProfileDto userProfileDto) {
        try {
            firebaseService.updateUserProfile(token, userProfileDto);
            return ResponseEntity.ok(new ApiResponse(true, "User profile updated successfully"));
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Error updating user profile"));
        }
    }


    @PostMapping("/questions")
    public ResponseEntity<?> postQuestion(@RequestBody QuestionDTO questionDTO) {
        try {
            if (questionDTO == null || questionDTO.getTitle() == null || questionDTO.getSubject() == null) {
                throw new IllegalArgumentException("Invalid request body");
            }

           
            String questionId = firebaseService.postQuestion(questionDTO);

            return ResponseEntity.ok(new ApiResponse(true, "Question posted successfully", questionId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

 
    @GetMapping("/questions/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable("id") String id) {
        try {
            
            QuestionDTO questionDTO = firebaseService.getQuestionById(id);

            return ResponseEntity.ok(new ApiResponse(true, "Question retrieved successfully", questionDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/questions")
    public ResponseEntity<?> getAllQuestions() {
        try {
           
            List<QuestionDTO> questionDTOs = firebaseService.getAllQuestions();

            return ResponseEntity.ok(new ApiResponse(true, "Questions retrieved successfully", questionDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, e.getMessage(), null));
        }
    }
       
    @PutMapping("/questions/{id}/upvote")
    public ResponseEntity<?> upvoteQuestion(@PathVariable("id") String id) {
        try {
          
            firebaseService.upvoteQuestion(id);
            
            return ResponseEntity.ok(new ApiResponse(true, "Question upvoted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/questions/{id}/downvote")
    public ResponseEntity<?> downvoteQuestion(@PathVariable("id") String id) {
        try {
            
            firebaseService.downvoteQuestion(id);
            
            return ResponseEntity.ok(new ApiResponse(true, "Question downvoted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/questions/{id}/answers")
    public ResponseEntity<?> postAnswer(@PathVariable("id") String id, @RequestBody AnswerDTO answerDTO) {
        try {
          
            if (answerDTO == null || answerDTO.getBody() == null) {
                throw new IllegalArgumentException("Invalid request body");
            }
            
           
            String answerId = firebaseService.postAnswer(id, answerDTO);
            
            return ResponseEntity.ok(new ApiResponse(true, "Answer posted successfully", answerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

   
    @GetMapping("/answers/{id}")
    public ResponseEntity<Answer> getAnswerById(@PathVariable(value = "id") String answerId)
            throws ResourceNotFoundException {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found for this id :: " + answerId));
        return ResponseEntity.ok().body(answer);
    }

    
    @PutMapping("/answers/{id}")
    public ResponseEntity<Answer> updateAnswer(@PathVariable(value = "id") String answerId,
                                                @Valid @RequestBody Answer answerDetails) throws ResourceNotFoundException {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found for this id :: " + answerId));

        answer.setAnswer(answerDetails.getAnswer());
        answer.setVotes(answerDetails.getVotes());
        answer.setAuthor(answerDetails.getAuthor());
        answer.setQuestionId(answerDetails.getQuestionId());

        final Answer updatedAnswer = answerRepository.save(answer);
        return ResponseEntity.ok(updatedAnswer);
    }

 
    @DeleteMapping("/answers/{id}")
    public Map<String, Boolean> deleteAnswer(@PathVariable(value = "id") String answerId)
            throws ResourceNotFoundException {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found for this id :: " + answerId));

        answerRepository.delete(answer);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }

    @PostMapping("/answers/{id}/upvote")
    public ResponseEntity<Answer> upvoteAnswer(@PathVariable(value = "id") String answerId)
            throws ResourceNotFoundException {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found for this id :: " + answerId));

        answer.setVotes(answer.getVotes() + 1);

        final Answer updatedAnswer = answerRepository.save(answer);
        return ResponseEntity.ok(updatedAnswer);
    }
        
    @PostMapping("/answers/{id}/downvote")
    public ResponseEntity<Answer> downvoteAnswer(@PathVariable(value = "id") String answerId)
            throws ResourceNotFoundException {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found for this id :: " + answerId));

        answer.setVotes(answer.getVotes() - 1);

        final Answer updatedAnswer = answerRepository.save(answer);
        return ResponseEntity.ok(updatedAnswer);
    }

 
    @PostMapping("/answers/{id}/comments")
    public ResponseEntity<Comment> createCommentOnAnswer(@PathVariable(value = "id") String answerId,
                                                        @Valid @RequestBody Comment comment) throws ResourceNotFoundException {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found for this id :: " + answerId));

        comment.setAnswerId(answerId);
        comment.setAuthor(comment.getAuthor());

        final Comment createdComment = commentRepository.save(comment);
        return ResponseEntity.ok(createdComment);
    }

  
    @GetMapping("/answers/{answerId}/comments")
    public List<Comment> getAllCommentsByAnswerId(@PathVariable(value = "answerId") String answerId) {
        Query query = commentsRef.orderByChild("answerId").equalTo(answerId);
        List<Comment> comments = new ArrayList<>();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    comments.add(comment);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw new RuntimeException(databaseError.getMessage());
            }
        });
        return comments;
    }


    @PostMapping("/answers/{answerId}/comments")
    public Comment addCommentToAnswer(@PathVariable(value = "answerId") String answerId,@RequestBody Comment comment) {
        comment.setAnswerId(answerId);
        String commentId = commentsRef.push().getKey();
        comment.setId(commentId);
        commentsRef.child(commentId).setValue(comment);
        return comment;
    }

    
    @PutMapping("/comments/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable(value = "id") String commentId,@Valid @RequestBody Comment commentDetails) throws ResourceNotFoundException {
        Comment comment = commentsRef.child(commentId).getValue(Comment.class);
        if (comment == null) {
            throw new ResourceNotFoundException("Comment not found for this id :: " + commentId);
        }
        comment.setText(commentDetails.getText());
        commentsRef.child(commentId).setValue(comment);
        return ResponseEntity.ok(comment);
    }


    @DeleteMapping("/comments/{id}")
    public Map<String, Boolean> deleteComment(@PathVariable(value = "id") String commentId) throws ResourceNotFoundException {
        Comment comment = commentsRef.child(commentId).getValue(Comment.class);
        if (comment == null) {
            throw new ResourceNotFoundException("Comment not found for this id :: " + commentId);
        }
        commentsRef.child(commentId).removeValue();
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }

    @GetMapping("/users/{userId}/notifications")
    public List<Notification> getNotificationsByUserId(@PathVariable(value = "userId") String userId) {
        Query query = notificationsRef.orderByChild("userId").equalTo(userId);
        List<Notification> notifications = new ArrayList<>();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot notificationSnapshot : dataSnapshot.getChildren()) {
                    Notification notification = notificationSnapshot.getValue(Notification.class);
                    notifications.add(notification);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw new RuntimeException(databaseError.getMessage());
            }
        });
        return notifications;

    }
    
    @GetMapping("/questions/search")
    public List<Question> searchQuestions(@RequestParam(value = "query") String query) {
        List<Question> questions = new ArrayList<>();
        QuerySnapshot querySnapshot = null;

        try {
            
            querySnapshot = FirestoreClient.getFirestore().collection("questions").whereArrayContains("subjects", query).get().get();

        
            for (QueryDocumentSnapshot document : querySnapshot) {
                Question question = document.toObject(Question.class);
                question.setId(document.getId());
                questions.add(question);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        try {
            
            querySnapshot = FirestoreClient.getFirestore().collection("questions").orderBy("timestamp", Query.Direction.DESCENDING).get().get();
            for (QueryDocumentSnapshot document : querySnapshot) {
                Question question = document.toObject(Question.class);
                question.setId(document.getId());
                if (question.getText().toLowerCase().contains(query.toLowerCase())) {
                    questions.add(question);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return questions;
    }}

