package org.acme.infrastructure.adapter.out.external;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.acme.application.port.out.UserServicePort;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@ApplicationScoped
@Slf4j
public class UserServiceAdapter implements UserServicePort {

    @Inject
    @RestClient
    UserServiceClient userServiceClient;

    @Override
    public boolean userExists(String userId) {
        try {
            log.debug("Checking if user exists: {}", userId);
            UserDto user = userServiceClient.getUserById(userId);
            return user != null && user.isActive;
        } catch (jakarta.ws.rs.NotFoundException e) {
            log.debug("User not found: {}", userId);
            return false;
        } catch (Exception e) {
            log.error("Error checking user existence: {}", userId, e);
            return false;
        }
    }

    @Override
    public UserDto getUserById(String userId) {
        try {
            log.debug("Fetching user by ID: {}", userId);
            return userServiceClient.getUserById(userId);
        } catch (Exception e) {
            log.error("Error fetching user: {}", userId, e);
            throw new IllegalArgumentException("Usuario no encontrado: " + userId);
        }
    }

    @Override
    public boolean areUsersFriends(String userId1, String userId2) {
        try {
            log.debug("Checking friendship between users: {} and {}", userId1, userId2);
            return userServiceClient.areUsersFriends(userId1, userId2);
        } catch (Exception e) {
            log.error("Error checking friendship: {} and {}", userId1, userId2, e);
            return false;
        }
    }

    @Override
    public List<String> getUserFriends(String userId) {
        try {
            log.debug("Fetching friends for user: {}", userId);
            return userServiceClient.getUserFriends(userId);
        } catch (Exception e) {
            log.error("Error fetching friends for user: {}", userId, e);
            return List.of();
        }
    }

    @RegisterRestClient(configKey = "user-service")
    public interface UserServiceClient {

        @GET
        @Path("/users/{userId}")
        @Produces(MediaType.APPLICATION_JSON)
        UserDto getUserById(@PathParam("userId") String userId);

        @GET
        @Path("/users/{userId1}/friends/{userId2}")
        @Produces(MediaType.APPLICATION_JSON)
        Boolean areUsersFriends(@PathParam("userId1") String userId1, @PathParam("userId2") String userId2);

        @GET
        @Path("/users/{userId}/friends")
        @Produces(MediaType.APPLICATION_JSON)
        List<String> getUserFriends(@PathParam("userId") String userId);
    }
}

