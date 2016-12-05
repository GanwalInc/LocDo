package com.ganwal.locationTodo.db;


import java.util.List;

public class User {
    private static final String TAG = User.class.getSimpleName();
    private transient long id;
    private String cloudId;
    private String googleId;
    private String name;
    private String email;
    //making fields transient so they won't be serialized
    private transient long lastLoginDate;
    private transient long createDate;
    private transient long lastUpdateDate;
    private transient boolean updated;
    private transient boolean deleted;
    private List<LocationTodo> userTodos;

    //---------------------------------------------------\\

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public long getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(long lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public boolean getUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public List<LocationTodo> getUserTodos() {
        return userTodos;
    }

    public void setUserTodos(List<LocationTodo> userTodos) {
        this.userTodos = userTodos;
    }

    //---------------------------------------------------\\

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        return getId() == user.getId();

    }

    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", cloudId=" + cloudId +
                ", googleId='" + googleId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createDate=" + createDate +
                ", lastUpdateDate=" + lastUpdateDate +
                ", lastLoginDate=" + lastLoginDate +
                ", updated=" + updated +
                ", deleted=" + deleted +
                ", userTodos=" + userTodos +
                '}';
    }
}
