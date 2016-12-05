package com.ganwal.locationTodoService;


import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;


@Entity
public class User {
    @JsonProperty("cloudId")
    @Id Long id;
    @Index String googleId;
    String name;
    String email;
    @Load @JsonIgnore
    List<Ref<LocationTodo>> userTodoRefs;

    //added for sending serialized todos to clients
    @Ignore
    List<LocationTodo> userTodos;

    public User() {}

    public User(String googleId, String name, String email) {
        this.googleId = googleId;
        this.name = name;
        this.email = email;
    }

    public User(Long id, String googleId, String name, String email) {
        this.id = id;
        this.googleId = googleId;
        this.name = name;
        this.email = email;
    }

    public User(Long cloudId, String googleId, String name, String email, List todos) {
        this.id = id;
        this.googleId = googleId;
        this.name = name;
        this.email = email;
        this.userTodoRefs = todos;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List getUserTodoRefs() {
        return userTodoRefs;
    }

    public void setUserTodoRefs(List userTodoRefs) {
        this.userTodoRefs = userTodoRefs;
    }

    public List<LocationTodo> getUserTodos() {
        return userTodos;
    }

    public void setUserTodos(List<LocationTodo> userTodos) {
        this.userTodos = userTodos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getId().equals(user.getId());
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", googleId='" + googleId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", userTodoRefs=" + userTodoRefs +
                ", userTodos=" + userTodos +
                '}';
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

}
