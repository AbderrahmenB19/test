package com.user19.pfe_testing.util;
import lombok.RequiredArgsConstructor;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KeycloakSecurityUtil {
    private final Keycloak keycloak;
    private final String realm_name="pfe";
    public  String getCurrentUserId() {
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getCredentials();
        return jwt.getClaim("sub");
    }
    public  String getCurrentUserName(String userId) {

        return keycloak.realm(realm_name).users().get(userId).toRepresentation().getLastName();
    }

    public Set<String> getCurrentUserRoles() {
        JwtAuthenticationToken  authentication =
                (JwtAuthenticationToken ) SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    public List<UserRepresentation> getUsersWithRoles(Set<String> roles) {
        return keycloak.realm(realm_name).users().list().stream()
                .filter(user -> hasAnyRole(user, roles))
                .collect(Collectors.toList());
    }

    private  boolean hasAnyRole(UserRepresentation user, Set<String> roles) {
        return keycloak.realm(realm_name).users().get(user.getId()).roles().realmLevel().listAll().stream()
                .anyMatch(role -> roles.contains(role.getName()));
    }

    public String getCurrentUserEmail(String userId) {
        return keycloak.realm(realm_name).users().get(userId).toRepresentation().getEmail();

    }
    public List<String> getValidatorsEmailsByRoles(Set<String> validatorsRoles){
        return getUsersWithRoles(validatorsRoles).stream().map(UserRepresentation::getEmail).toList();
    }


    public String getUserNameById(String actorId) {
        return keycloak.realm(realm_name).users().get(actorId).toRepresentation().getUsername();
    }
}