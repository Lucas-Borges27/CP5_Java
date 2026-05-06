package br.com.fiap.produtosms.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public final class GitHubUserUtils {

    private GitHubUserUtils() {
    }

    public static String getUsername(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return "";
        }
        if (authentication instanceof OAuth2AuthenticationToken oauth2AuthenticationToken) {
            Object login = oauth2AuthenticationToken.getPrincipal().getAttributes().get("login");
            return login == null ? "" : login.toString();
        }
        return authentication.getName();
    }

    public static String getAvatar(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2AuthenticationToken)
                || oauth2AuthenticationToken.getPrincipal() == null) {
            return "";
        }
        Object avatar = oauth2AuthenticationToken.getPrincipal().getAttributes().get("avatar_url");
        return avatar == null ? "" : avatar.toString();
    }
}
