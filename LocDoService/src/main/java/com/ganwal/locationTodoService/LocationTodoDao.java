package com.ganwal.locationTodoService;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class LocationTodoDao {

    private static final Logger log = Logger.getLogger(LocationTodoDao.class.getSimpleName());
    static {
        ObjectifyService.register(User.class);
        ObjectifyService.register(LocationTodo.class);
    }

    public List<User> getUsers(String gId) {
        gId = gId.trim();
        log.fine("getUsers: gId:" + gId);
        List users = new ArrayList<User>();
        if(gId != null && gId.length() > 0) {
            User user = ObjectifyService.ofy().load().type(User.class).filter("googleId", gId).first().now();
            log.fine("getUsers: user:" + user);
            if(user != null) {
                user.setUserTodos(getUserTodos(user.getId()));
                log.fine("getUsers: user todos:" + user.getUserTodos());
                users.add(user);
            }
        }
        return users;
    }

    public User getUserById(Long id) {
        User user = ObjectifyService.ofy().load().type(User.class).id(id).now();
        log.fine("getUserById: user:" + user);
        if(user != null && user.getId() > 0) {
            user.setUserTodos(getUserTodos(user.getId()));
            log.fine("getUserById: user todos:" + user.getUserTodos());
        }
        return user;
    }

    public List<LocationTodo> getUserTodos(Long userId) {
        User user = ObjectifyService.ofy().load().type(User.class).id(userId).now();
        List<Ref<LocationTodo>> todoRefList = user.getUserTodoRefs();
        List todoList = null;
        if(todoRefList != null && todoRefList.size() > 0) {
            List keys = new ArrayList();
            //TODO - adding this code below to make sure the LocationTodo is loaded, right way to do
            //this is to use the @Load with List<Ref<LocationTodo>> annotation but it is causing cyclic references
            for (Ref todoRef : todoRefList) {
                log.fine("getUserTodos ref class:" + todoRef);
                keys.add(todoRef.getKey());
            }
            log.fine("getUserTodos keys:" + keys);
            if(keys != null && keys.size() > 0) {
                todoList = new ArrayList<>(ObjectifyService.ofy().load().keys(keys).values());
            }
        }
        log.fine("getUserTodos todoList:" + todoList);
        return todoList;
    }

    public LocationTodo getTodoById(Long todoId) {
        return ObjectifyService.ofy().load().type(LocationTodo.class).id(todoId).now();
    }

    public Long saveUser(User user) {
        ObjectifyService.ofy().save().entity(user).now();
        return user.getId();
    }

    public Long createToDo(Long userId, LocationTodo todo) {

        log.fine("createToDo: todo:"+todo);
        log.fine("createToDo: todo userId:"+userId);
        User user = ObjectifyService.ofy().load().type(User.class).id(userId).now();
        //TODO - Is there a way to make user as part of todo key, so everything is saved at
        // one place like entity group
        log.fine("createToDo: retrieved user:"+user);

        if(user == null) {
            return -1l;
        }
        List<Ref<LocationTodo>> todoList = user.getUserTodoRefs();
        if(todoList == null) {
            todoList = new ArrayList<Ref<LocationTodo>>();
        }
        //first save the task
        ObjectifyService.ofy().save().entity(todo).now();
        todoList.add(Ref.create(todo));
        //now update the task reference in the user table
        user.setUserTodoRefs(todoList);
        ObjectifyService.ofy().save().entity(user).now();
        return todo.getId();
    }

    public boolean updateToDo(LocationTodo todo) {
        ObjectifyService.ofy().save().entity(todo).now();
        return true;
    }


    public boolean deleteTodoById(Long userId, Long todoId) {
        log.fine("deleteTodoById: userId:"+userId);
        log.fine("deleteTodoById: todoId:"+todoId);
        User user = ObjectifyService.ofy().load().type(User.class).id(userId).now();
        LocationTodo todo = ObjectifyService.ofy().load().type(LocationTodo.class).id(todoId).now();
        List<Ref<LocationTodo>> todoList = user.getUserTodoRefs();
        log.fine("deleteTodoById: user:"+user);
        //TODO - Is there better way of doing this, below code for loop?
        if(todoList != null && todoList.size() > 0) {
            for(int i=0; i<todoList.size(); i++) {
                log.fine("deleteTodoById: ref class:" + todoList.get(i).getClass());
                log.fine("deleteTodoById: ref loaded:" + todoList.get(i).isLoaded());
                Long id = todoList.get(i).getKey().getId();
                log.fine("deleteTodoById: ref loaded object by key id :"+id);
                log.fine("deleteTodoById: ref loaded object by key:"+ObjectifyService.ofy().load().key(todoList.get(i).key()));

                log.fine("deleteTodoById: cloudId loaded:" + id);
                if (id.equals(todoId)) {
                    log.fine("deleteTodoById: result:" + todoList.remove(todoList.get(i)));
                    ObjectifyService.ofy().save().entity(user).now();
                }
            }
        }
        ObjectifyService.ofy().delete().entity(todo).now();
        return true;
    }

}
