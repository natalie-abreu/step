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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;


import com.google.sps.data.Comment;

@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {

    static String ID_PARAM = "id";

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
       
        long id = Long.parseLong(request.getParameter(ID_PARAM));
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query;
        if (id != -1) {
            query = new Query(Comment.DATA_TYPE).setFilter(new FilterPredicate("manual_id", FilterOperator.EQUAL, id));
        }
        else {
            query = new Query(Comment.DATA_TYPE);
        }

        PreparedQuery results = datastore.prepare(query);

        for (Entity entity : results.asIterable()) {
            datastore.delete(entity.getKey());
        }
        response.getWriter().println();
    }
}
