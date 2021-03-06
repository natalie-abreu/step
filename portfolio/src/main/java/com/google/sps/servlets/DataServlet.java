// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;
import com.google.gson.*;
import com.google.sps.data.Comment;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Key;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.User;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import java.io.IOException;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.TranslateException;


/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  static String MAX_COMMENTS_PARAM = "max";
  static String PAGE_NUM_PARAM = "page";
  static String SORT_BY_PARAM = "sort";
  static String LANGUAGE_PARAM = "lang";
  static String COMMENT_NUM = "CommentNum";
  static String COMMENT_NUM_VALUE = "value";
  List<String> LANGUAGE_CODES = Arrays.asList("en", "es", "zh", "hi", "ar", "fr");

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int pageSize = getNumComments(request);
    int pageNum = Integer.parseInt(request.getParameter(PAGE_NUM_PARAM));
    String sortBy = request.getParameter(SORT_BY_PARAM);
    String languageCode = request.getParameter(LANGUAGE_PARAM);
    if (!LANGUAGE_CODES.contains(languageCode)) languageCode = "en";
    

    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize).offset(pageSize*(pageNum-1));
    Query query = new Query(Comment.DATA_TYPE).addSort(sortBy, SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(query);

    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);

    // page invalid
    if (pageNum <= 0 || (pageNum != 1 && results.size() == 0)) {
        System.out.println("PAGE " + pageNum + " INVALID");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
    }
 
    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results) {
        long id = (long) entity.getProperty(Comment.ID);
        String author = (String) entity.getProperty(Comment.AUTHOR);
        long timestamp = (long) entity.getProperty(Comment.TIMESTAMP);
        String message = (String) entity.getProperty(Comment.CONTENT);
        message = translateMessage(languageCode, message);
        String userId = (String) entity.getProperty(Comment.USER_ID);
        double sentimentScore = (double) entity.getProperty(Comment.SENTIMENT_SCORE);

        Comment comment = new Comment(id, author, timestamp, message, userId, sentimentScore);
        comments.add(comment);
    }

    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");

    UserService userService = UserServiceFactory.getUserService();
    User currentUser = userService.getCurrentUser();

    String json = convertToJson(comments, currentUser);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String newComment = request.getParameter("user-input");
      long timestamp = System.currentTimeMillis();

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      Query query = new Query(COMMENT_NUM);
      PreparedQuery pq = datastore.prepare(query);
      QueryResultList<Entity> commentNumResult = pq.asQueryResultList(FetchOptions.Builder.withDefaults());

      Entity commentNumEntity;
      long commentNumValue;
      if (commentNumResult.size() == 0) {
          commentNumEntity = new Entity(COMMENT_NUM);
          commentNumValue = 0;
          commentNumEntity.setProperty(COMMENT_NUM_VALUE, commentNumValue);
          datastore.put(commentNumEntity);      
      }
      else {
          commentNumEntity = commentNumResult.get(0);
          commentNumValue = (long)commentNumEntity.getProperty(COMMENT_NUM_VALUE)+1;
          commentNumEntity.setProperty(COMMENT_NUM_VALUE, commentNumValue);
          datastore.put(commentNumEntity);
      }

      UserService userService = UserServiceFactory.getUserService();
      User currentUser = userService.getCurrentUser();
      String userId = currentUser.getUserId();
      String name = currentUser.getNickname();
      double sentimentScore = analyzeSentiment(newComment);

      Entity commentEntity = new Entity(Comment.DATA_TYPE);
      commentEntity.setProperty(Comment.CONTENT, newComment);
      commentEntity.setProperty(Comment.TIMESTAMP, timestamp);
      commentEntity.setProperty(Comment.AUTHOR, name);
      commentEntity.setProperty(Comment.ID, commentNumValue);
      commentEntity.setProperty(Comment.USER_ID, userId);
      commentEntity.setProperty(Comment.SENTIMENT_SCORE, sentimentScore);

      datastore.put(commentEntity);

      response.sendRedirect("/comments.html");
  }

  private String convertToJson(List<Comment> comments, User user) {
      String userJson = convertUserToJsonUsingGson(user);
      String commentsJson = convertCommentToJsonUsingGson(comments);
      return mergeJson(userJson, commentsJson);
  }

  private String convertCommentToJsonUsingGson(List<Comment> comments) {
      Gson gson = new Gson();
      String json = gson.toJson(comments);
      return json;
  }

  private String convertUserToJsonUsingGson(User user) {
      Gson gson = new Gson();
      String json = gson.toJson(user);
      return json;
  }

  private String mergeJson(String user, String comments) {
      String json = "{";
      json += "\"user\": ";
      json += user;
      json += ", ";
      json += "\"comments\": ";
      json += comments;
      json += "}";
      return json;
  }

  private int getNumComments(HttpServletRequest request) {
      String userInput = request.getParameter(MAX_COMMENTS_PARAM);
      int numComments;
      try {
        numComments = Integer.parseInt(userInput);
      } catch (NumberFormatException e) {
        System.err.println("Could not convert to int: " + userInput);
        // defaults to showing 5 comments
        return 5;
      }
      return numComments;
  }

  private double analyzeSentiment(String message) throws IOException {
      Document doc =
        Document.newBuilder().setContent(message).setType(Document.Type.PLAIN_TEXT).build();
      LanguageServiceClient languageService = LanguageServiceClient.create();
      Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
      double score = sentiment.getScore();
      languageService.close();
      return score;
  }

  private String translateMessage(String languageCode, String message) throws IOException {
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        Translation translation;
        try {
            translation = translate.translate(message, Translate.TranslateOption.targetLanguage(languageCode));
        } catch (TranslateException e) {
            translation = translate.translate(message, Translate.TranslateOption.targetLanguage("en"));
        }
        String translatedText = translation.getTranslatedText();
        return translatedText;
  }
}