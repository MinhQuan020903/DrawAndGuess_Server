package com.backend.rest.friend;

import com.backend.rest.user.User;
import com.backend.rest.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;

    public List<User> getFriendList(String username) {
        Optional<User> matchingUser = userRepository.findByUsername(username);
        if (matchingUser.isPresent()) {
            List<User> friends = new ArrayList<>();
            for (String friendUsername : matchingUser.get().getFriendList()) {
                Optional<User> friend = userRepository.findByUsername(friendUsername);
                friend.ifPresent(friends::add);
            }
            return friends;
        }
        return new ArrayList<>();
    }

    public List<User> getFriendRequests(String username) {
        Optional<User> matchingUser = userRepository.findByUsername(username);
        if (matchingUser.isPresent()) {
            List<User> friends = new ArrayList<>();
            for (String friendUsername : matchingUser.get().getFriendRequests()) {
                Optional<User> friend = userRepository.findByUsername(friendUsername);
                friend.ifPresent(friends::add);
            }
            return friends;
        }
        return new ArrayList<>();
    }

    public List<User> getFriendRequestsReceive(String username) {
        Optional<User> matchingUser = userRepository.findByUsername(username);
        if (matchingUser.isPresent()) {
            List<User> friends = new ArrayList<>();
            for (String friendUsername : matchingUser.get().getFriendRequestsReceive()) {
                Optional<User> friend = userRepository.findByUsername(friendUsername);
                friend.ifPresent(friends::add);
            }
            return friends;
        }
        return new ArrayList<>();
    }

    public void addFriendRequest(String senderUsername, String receiverUsername) throws Exception {

        if (senderUsername.equals(receiverUsername)) {
            throw new Exception("The sender must be different from the recipient");
        }

        Optional<User> sender = userRepository.findByUsername(senderUsername);
        Optional<User> receiver = userRepository.findByUsername(receiverUsername);

        if (sender.isPresent()) {
            if (receiver.isPresent()) {
                // Xử lý người gửi
                User getSender = sender.get();
                if (getSender.getFriendRequests().contains(receiverUsername)) {
                    return;
                }
                getSender.addFriendRequest(receiverUsername);
                userRepository.save(getSender);
                // Xử lý người nhận
                User getReceiver = receiver.get();
                if (getReceiver.getFriendRequestsReceive().contains(senderUsername)) {
                    return;
                }
                getReceiver.addFriendRequestReceive(senderUsername);
                userRepository.save(getReceiver);
            } else {
                throw new Exception("Receiver User not found");
            }
        }
        else {
            throw new Exception("Sender User not found");
        }
    }

    public void acceptFriendRequest(String senderUsername, String receiverUsername) throws Exception {

        if (senderUsername.equals(receiverUsername)) {
            throw new Exception("The sender must be different from the recipient");
        }

        Optional<User> sender = userRepository.findByUsername(senderUsername);
        Optional<User> receiver = userRepository.findByUsername(receiverUsername);

        if (sender.isPresent()) {
            if (receiver.isPresent()) {
                // Xử lý ở người gửi
                User getSender = sender.get();
                if (!getSender.getFriendRequests().contains(receiverUsername)) {
                    throw new Exception("Friend Requests List of " + senderUsername + " doesn't contain " + receiverUsername);
                }
                getSender.removeFriendRequest(receiverUsername);
                getSender.addFriendList(receiverUsername);
                userRepository.save(getSender);
                // Xử lý ở người nhận
                User getReceiver = receiver.get();
                if (!getReceiver.getFriendRequestsReceive().contains(senderUsername)) {
                    throw new Exception("Friend Requests List of " + receiverUsername + " doesn't contain " + senderUsername);
                }
                getReceiver.removeFriendRequestReceive(senderUsername);
                getReceiver.addFriendList(senderUsername);
                userRepository.save(getReceiver);
            } else {
                throw new Exception("Receiver User not found");
            }
        }
        else {
            throw new Exception("Sender User not found");
        }
    }

}
