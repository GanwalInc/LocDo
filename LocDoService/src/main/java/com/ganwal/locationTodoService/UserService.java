package com.ganwal.locationTodoService;


import com.googlecode.objectify.ObjectifyService;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * List of this webservice endpoints:
 * /users?gId= - GET - gets all the users by Google ID
 * /users - POST - insert user
 * /users/ID - GET - get user with ID
 * /users/ID - PUT - updates user with ID
 * /users/ID - DELETE - deletes user with ID
 *
 * /users/ID/todos - GET - gets all the todos for this user
 * /users/ID/todos - POST - insert task for this user
 * /users/ID/todos/ID - GET - get task for user
 * /users/ID/todos/ID - PUT - updates task for user
 * /users/ID/todos/ID - DELETE - deletes task for user
 *
 */

@LoggedIn
@Path("/users")
public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class.getSimpleName());
    private LocationTodoDao todoDao = new LocationTodoDao();

    static {
        ObjectifyService.register(User.class);
        ObjectifyService.register(LocationTodo.class);
    }


    @GET @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    public Response findUserById(@PathParam("id") Long id) {
        User user = todoDao.getUserById(id);
        if(user != null) {
            return Response.status(200).entity(user).build();
        } else {
            return Response.status(404).entity("The user with the id " + id + " does not exist").build();
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    public Response findUsers(@QueryParam("gId") String gId) {
        List<User> users = todoDao.getUsers(gId.trim());
        if(users != null && users.size() > 0) {
            return Response.status(200).entity(users).build();
        } else {
            return Response.status(404).entity("The user with the cloudId " + gId + " does not exist").build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(User user) {
        log.fine("createUser: user:"+user);
        if(user == null) {
            return Response.status(400).entity("Invalid user data:"+user).build();
        }
        //insert user to db
        Long id = todoDao.saveUser(user);
        return Response.status(201).entity(user).build();
    }

    @POST
    @Path("{id}/todos/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createToDoByUser(@PathParam("id") Long id,
                                     LocationTodo todo) {
        log.fine("createToDoByUser: todo:" + todo);
        if (todo == null) {
            return Response.status(400).entity("Invalid todo data:" + todo).build();
        }
        log.fine("createToDoByUser: Inserting todo");
        //insert user to db
        if (todoDao.createToDo(id, todo) != null) {
            return Response.status(201).entity(todo).build();
        }
        return Response.status(500).entity("Error creating todo").build();
    }


    @PUT
    @Path("{id}/todos/{todo_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateToDoByUser(@PathParam("id") Long id,
                                     @PathParam("todo_id") Long todo_id,
                                     LocationTodo todo) {
        log.fine("updateToDoByUser: id:"+id);
        log.fine("updateToDoByUser: todo_id:"+todo_id);
        log.fine("updateToDoByUser: todo:"+todo);
        if (todo == null) {
            return Response.status(400).entity("Invalid todo data:" + todo).build();
        }
        if(todo_id != null && todo != null) {
            //update todos
            log.fine("updateToDoByUser: Updating todo");
            if(todoDao.updateToDo(todo)) {
                return Response.status(200).entity(todo).build();
            }
        }
        return Response.status(500).entity("Error updating todo").build();
    }

    @GET
    @Path("{id}/todos/{todo_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response findTodoById(@PathParam("id") Long id,
                             @PathParam("todo_id") Long todo_id) {
        log.fine("findTodoById: cloudId:"+id);
        log.fine("findTodoById: todo_id:"+todo_id);
        LocationTodo todo = todoDao.getTodoById(todo_id);
        log.fine("findTodoById: todo:"+todo);
        if(todo != null) {
            return Response.status(200).entity(todo).build();
        } else {
            return Response.status(404).entity("The todo with the cloudId " + id + " does not exist").build();
        }
    }


    @GET
    @Path("{id}/todos")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response findTodosByUser(@PathParam("id") Long id) {
        log.fine("findTodosByUser: id:"+id);
        List<LocationTodo> todos = todoDao.getUserTodos(id);
        log.fine("findTodosByUser: todos:"+todos);
        if(todos != null) {
            return Response.status(200).entity(todos).build();
        } else {
            return Response.status(404).entity("The todos for userId" + id + " does not exist").build();
        }
    }


    @DELETE
    @Path("{id}/todos/{todo_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteToDoByUser(@PathParam("id") Long id,
                                     @PathParam("todo_id") Long todo_id) {
        log.fine("deleteToDoByUser: id:"+id);
        log.fine("deleteToDoByUser: todo_id:"+todo_id);
        if(todo_id != null ) {
            //update todos
            log.fine("deleteToDoByUser: deleting todo");
            if(todoDao.deleteTodoById(id, todo_id)) {
                return Response.status(204).build();
            }
        } else {
            return Response.status(404).entity("The todo for Id" + todo_id+ " does not exist").build();
        }
        return Response.status(500).entity("Error deleting todo").build();
    }


}