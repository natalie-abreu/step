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



import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  static String DATA_TYPE = "Comment";
  static String COMMENT_TIMESTAMP = "timestamp";
  static String COMMENT_AUTHOR = "name";
  static String COMMENT_CONTENT = "message";
  static String MAX_COMMENTS_PARAM = "max";
  static String PAGE_NUM_PARAM = "page";
  static int PAGE_SIZE;
  static int PAGE_NUM;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PAGE_SIZE = getNumComments(request);
    PAGE_NUM = Integer.parseInt(request.getParameter(PAGE_NUM_PARAM));

    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(PAGE_SIZE*PAGE_NUM);

    Query query = new Query(DATA_TYPE).addSort(COMMENT_TIMESTAMP, SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(query);

    QueryResultList<Entity> results;
    results = pq.asQueryResultList(fetchOptions);

    // page invalid
    if (PAGE_NUM <= 0 || (results.size() != 0 && Math.ceil(results.size() / (double)PAGE_SIZE) < PAGE_NUM)) {
        System.out.println("PAGE " + PAGE_NUM + " INVALID");
        return;
    }
    // find how many comments should be displayed
    int numResults = results.size() % PAGE_SIZE;
    if (numResults == 0 && results.size() != 0) numResults = PAGE_SIZE;

    List<Comment> comments = new ArrayList<>();
    for (int i = results.size()-numResults; i < results.size(); i++) {
        Entity entity = results.get(i);
        long id = entity.getKey().getId();
        String author = (String) entity.getProperty(COMMENT_AUTHOR);
        long timestamp = (long) entity.getProperty(COMMENT_TIMESTAMP);
        String message = (String) entity.getProperty(COMMENT_CONTENT);

        Comment comment = new Comment(id, author, timestamp, message);
        comments.add(comment);
    }

    response.setContentType("application/json;");
    String json = convertToJsonUsingGson(comments);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String name = request.getParameter("name-input");
      String newComment = request.getParameter("user-input");
      long timestamp = System.currentTimeMillis();

      Entity commentEntity = new Entity(DATA_TYPE);
      commentEntity.setProperty(COMMENT_CONTENT, newComment);
      commentEntity.setProperty(COMMENT_TIMESTAMP, timestamp);
      commentEntity.setProperty(COMMENT_AUTHOR, name);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);

      response.sendRedirect("/comments.html");
  }

  private String convertToJsonUsingGson(List<Comment> messages) {
      Gson gson = new Gson();
      String json = gson.toJson(messages);
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
}