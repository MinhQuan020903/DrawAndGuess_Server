package com.backend.rest.user;

import com.backend.rest.room.entity.Room;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private int id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "is_online")
    private boolean isOnline;

    @Column(name = "friend_list")
    private List<String> friendList;

    @Column(name = "friend_requests")
    private List<String> friendRequests;

    @Column(name = "friend_requests_receive")
    private List<String> friendRequestsReceive;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = true, referencedColumnName = "room_id")
    @JsonBackReference
    // cart_id chính là trường khoá phụ trong table Items liên kết với khoá chính trong table Cart
    private Room room;

    public void addFriendList(String username) {
        friendList.add(username);
    }

    public void removeFriendList(String username) {
        friendList.remove(username);
    }

    public void addFriendRequest(String username) {
        friendRequests.add(username);
    }

    public void removeFriendRequest(String username) {
        friendRequests.remove(username);
    }

    public void addFriendRequestReceive(String username) {
        friendRequestsReceive.add(username);
    }

    public void removeFriendRequestReceive(String username) {
        friendRequestsReceive.remove(username);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public int getId() {return id;}

    public Role getRole() {return role;}

    public String getDisplayName() {return displayName;}

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
