package com.ganwal.locationTodoService;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.net.URI;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocationTodoServiceTest {

    WebTarget mTarget;
    static String USER_URL_DIR = "users";
    static String TODO_URL_DIR = "todos";

    private static URI getBaseURI() {
        return UriBuilder.fromUri("https://locationtodoservice.appspot.com/rest/").build();
    }

    @Before
    public void initialize() {
        mTarget = ClientBuilder.newBuilder()
                .register(JacksonObjectMapperProvider.class)
                .register(JacksonFeature.class)
                .build().target(getBaseURI());
    }




    @Test
    public void testGetUserById() {
        //insert user first
        User user = new User("GI","G Inc", "ganwalinc@gmail.com");
        Response response = mTarget.path(USER_URL_DIR).
                request().
                post(Entity.entity(user,MediaType.APPLICATION_JSON),Response.class);
        System.out.println("Returned Response:"+response.getEntity());
        //update our user object
        user = response.readEntity(User.class);
        System.out.println("Returned User:"+user);
        //now get it
        Long userId = new Long("5675267779461120");
        response = mTarget.path(USER_URL_DIR).
                path(userId.toString()).
                request().
                accept(MediaType.APPLICATION_JSON).get();
        System.out.println("Returned Response:"+response);
        System.out.println("Returned Entity:"+response.getEntity());
        User returnedUser = response.readEntity(User.class);
        System.out.println("Returned User:"+returnedUser);
        org.junit.Assert.assertNotNull(returnedUser);
    }


    @Test
    public void testGetUsers() {
        //insert user first
        User user = new User("GI","G Inc", "ganwalinc@gmail.com");
        Response response = mTarget.path(USER_URL_DIR).
                request().
                post(Entity.entity(user,MediaType.APPLICATION_JSON),Response.class);
        System.out.println("Returned Response:"+response.getEntity());
        //update our user object
        user = response.readEntity(User.class);
        System.out.println("Inserted User:"+user);
        //now get by googleId
        Response response2 = mTarget.path(USER_URL_DIR).
                queryParam("gId", "115686073333165319775").
                request().
                header(HttpHeaders.AUTHORIZATION, "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IjNiNjc1YzMzODM2YzMyZTAxYTBmNjcyMWRlYmU0ODlmNzljYzM4YzgifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhdWQiOiI2NzgzNTMxNTk3Mzgta2hkamNranRiZm9pOW5pMWo1bGhoOTg2azk1amJrOTQuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTU2ODYwNzMzMzMxNjUzMTk3NzUiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXpwIjoiNjc4MzUzMTU5NzM4LXUwM3ZtaWkwNWJhdnFpMGZlZDZpcjl1c3RoOXV2MXRpLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJnYW53YWxpbmNAZ21haWwuY29tIiwiaWF0IjoxNDc1MzQ4OTQ1LCJleHAiOjE0NzUzNTI1NDUsIm5hbWUiOiJHYW53YWwgSW5jIiwicGljdHVyZSI6Imh0dHBzOi8vbGg0Lmdvb2dsZXVzZXJjb250ZW50LmNvbS8tUzV4RXlKbGFod0UvQUFBQUFBQUFBQUkvQUFBQUFBQUFBQUEvQVBhWEhoU3Zvdzd0cTRfOXcxLVhFVFJQWnBFX2hZZi1TUS9zOTYtYy9waG90by5qcGciLCJnaXZlbl9uYW1lIjoiR2Fud2FsIiwiZmFtaWx5X25hbWUiOiJJbmMiLCJsb2NhbGUiOiJlbiJ9.MHg4Yah8GIagSmoiPkiyD_GtcLZ7_4kz5MFINUCJuOC3yYiNBkLXeBLiTxR2BmtABAU2mNY2598oClWhWxb-tEaGmGPn9fYuV5xLG0zSytVP7laxajC1CTf6B2GzYHcH9rvd4CxA95G000vHX_hRHz2r81C4PtvvloHI-8SiFFj0Q_IFRwJqIhl7Qz7BdybFVZyj_1L_SpOTWlsdgEQB6Rw-CX6FzfLUIydYy-yyUHJ6x8IT1fzvDH8x4QWDqyN-ksaAjQXVYiPeJj4Fbuhmu4DrHfYNmLWq9XZKVr-pYpFnP2u5ceENR9y_LKjnkGu99I_BN37FqK9qaNFtaYB-sw").
                accept(MediaType.APPLICATION_JSON).get();
        System.out.println("Returned Response:"+response2);
        List<User> returnedUsers= response2.readEntity(new GenericType<List<User>>(){});
        System.out.println("Returned returnedUsers:"+returnedUsers);
        org.junit.Assert.assertNotNull(returnedUsers);
    }

    @Test
    public void testCreatingUserToDo() {
        //insert user first
        User user = new User("GI","G Inc", "ganwalinc@gmail.com");
        Response response = mTarget.path(USER_URL_DIR).
                request().
                post(Entity.entity(user,MediaType.APPLICATION_JSON),Response.class);
        System.out.println("Returned Response:"+response.getEntity());
        //update our user object
        user = response.readEntity(User.class);
        System.out.println("Returned User:"+user);
        //create todos for user
        LocationTodo task = new LocationTodo();
        task.setName("todo1");
        task.setSummary("testing again");
        task.setLocationAlert(false);
        task.setGeofenceID(1l);
        response = mTarget.path(USER_URL_DIR).path(user.getId().toString()).
                path(TODO_URL_DIR).
                request().
                post(Entity.entity(task,MediaType.APPLICATION_JSON),Response.class);
        System.out.println("Returned Response:"+response.getEntity());
        LocationTodo returnedTodo = response.readEntity(LocationTodo.class);
        System.out.println("Returned task:"+returnedTodo);
        org.junit.Assert.assertNotNull(returnedTodo);
    }

    @Test
    public void testGetUserToDo() {
        //insert user first
        User user = new User("GI","G Inc", "ganwalinc@gmail.com");
        Response response = mTarget.path(USER_URL_DIR).
                request().
                post(Entity.entity(user,MediaType.APPLICATION_JSON),Response.class);
        System.out.println("Returned Response:"+response.getEntity());
        //update our user object
        user = response.readEntity(User.class);
        System.out.println("Returned User:"+user);
        System.out.println("Returned User.getId:"+user.getId());

        //create todos for user
        LocationTodo task = new LocationTodo();
        task.setName("todo1");
        task.setSummary("testing again");
        task.setLocationAlert(false);
        task.setGeofenceID(1l);
        response = mTarget.path(USER_URL_DIR).path(user.getId().toString()).
                path(TODO_URL_DIR).
                request().
                post(Entity.entity(task,MediaType.APPLICATION_JSON),Response.class);
        System.out.println("Returned Response:"+response.getEntity());
        LocationTodo returnedTodo = response.readEntity(LocationTodo.class);
        System.out.println("Returned todo:"+returnedTodo);


        //get todos for user
         response = mTarget.path(USER_URL_DIR).path(user.getId().toString()).
                path(TODO_URL_DIR).
                path(returnedTodo.getId().toString()).
                request().
                accept(MediaType.APPLICATION_JSON).get();
        System.out.println("Returned Response:"+response);
        System.out.println("Returned Entity:"+response.getEntity());
        returnedTodo = response.readEntity(LocationTodo.class);
        System.out.println("Returned todo:"+returnedTodo);
        org.junit.Assert.assertNotNull(returnedTodo);
    }

    @Test
    public void testGetToDosByUser() {
        //insert user first
        User user = new User("GI","G I", "ganwalinc@gmail.com");
        Response response = mTarget.path(USER_URL_DIR).
                request().
                post(Entity.entity(user,MediaType.APPLICATION_JSON),Response.class);
        System.out.println("Returned Response:"+response.getEntity());
        //update our user object
        user = response.readEntity(User.class);
        System.out.println("Returned User:"+user);

        //create todos for user
        LocationTodo task = new LocationTodo();
        task.setName("todo1");
        task.setSummary("testing again");
        task.setLocationAlert(false);
        task.setGeofenceID(1l);
        response = mTarget.path(USER_URL_DIR).path(user.getId().toString()).
                path(TODO_URL_DIR).
                request().
                post(Entity.entity(task,MediaType.APPLICATION_JSON),Response.class);
        System.out.println("Returned Response:"+response.getEntity());
        LocationTodo returnedTodo = response.readEntity(LocationTodo.class);
        System.out.println("Returned task:"+returnedTodo);


        //Response response = mTarget.path(USER_URL_DIR).path(user.getId().toString()).
        response = mTarget.path(USER_URL_DIR).path(new Long("5675267779461120").toString()).
                path(TODO_URL_DIR).
                request().
                accept(MediaType.APPLICATION_JSON).get();
        System.out.println("Returned Response:"+response);
        List<LocationTodo> returnedTodos = response.readEntity(new GenericType<List<LocationTodo>>(){});
        System.out.println("Returned todos:"+returnedTodos);
        org.junit.Assert.assertNotNull(returnedTodos);
    }



    public void testUpdateToDo() {
        Long userId = new Long(5147289865682944l);
        Long todoId = new Long(5068433729257472l);
        LocationTodo task = new LocationTodo();
        task.setId(todoId);
        task.setName("todo2");
        task.setSummary("testing again & again & again");

        final Response response = mTarget.path(USER_URL_DIR).path(userId.toString()).
                path(TODO_URL_DIR).
                path(todoId.toString()).
                request().put(Entity.entity(task,MediaType.APPLICATION_JSON),Response.class);
        System.out.println("Returned Response:"+response);
        System.out.println("Returned Entity:"+response.getEntity());
        LocationTodo returnedTodo = response.readEntity(LocationTodo.class);
        System.out.println("Returned todo:"+returnedTodo);
        org.junit.Assert.assertNotNull(returnedTodo);
    }


    public void testDeleteToDo() {
        Long userId = new Long(5111418835697664l);
        Long todoId = new Long(5767281011326976l);
        final Response response = mTarget.path(USER_URL_DIR).path(userId.toString()).
                path(TODO_URL_DIR).
                path(todoId.toString()).
                request().delete();
        //.post(Entity.json(user));
        System.out.println("Returned Response:"+response);
        org.junit.Assert.assertNotNull(response);
    }

}
