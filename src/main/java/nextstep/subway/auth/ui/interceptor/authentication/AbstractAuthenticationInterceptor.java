package nextstep.subway.auth.ui.interceptor.authentication;

import nextstep.subway.auth.application.converter.AuthenticationConverter;
import nextstep.subway.auth.domain.Authentication;
import nextstep.subway.auth.domain.AuthenticationToken;
import nextstep.subway.member.application.CustomUserDetailsService;
import nextstep.subway.member.domain.LoginMember;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractAuthenticationInterceptor implements HandlerInterceptor {

    private final AuthenticationConverter converter;
    private final CustomUserDetailsService userDetailsService;

    protected AbstractAuthenticationInterceptor(AuthenticationConverter converter, CustomUserDetailsService userDetailsService) {
        this.converter = converter;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        AuthenticationToken token = converter.convert(request);
        Authentication authentication = authenticate(token);
        afterAuthentication(request, response, authentication);
        return false;
    }

    protected abstract void afterAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException;

    private Authentication authenticate(AuthenticationToken token) {
        final String principal = token.getPrincipal();
        final LoginMember loginMember = userDetailsService.loadUserByUsername(principal);
        checkAuthentication(loginMember, token);

        return new Authentication(loginMember);
    }

    private void checkAuthentication(LoginMember loginMember, AuthenticationToken token) {
        if (loginMember == null) {
            throw new RuntimeException("No such user in repository.");
        }

        if (!loginMember.checkPassword(token.getCredentials())) {
            throw new RuntimeException("Wrong password.");
        }
    }
}