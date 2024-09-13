package com.techcourse.controller;

import com.techcourse.controller.dto.LoginRequestDto;
import com.techcourse.db.InMemoryUserRepository;
import com.techcourse.exception.SafeExecutionWrapper;
import com.techcourse.exception.UnauthorizedException;
import com.techcourse.model.LoginCredentials;
import com.techcourse.model.User;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.apache.catalina.controller.AbstractSessionController;
import org.apache.catalina.controller.Handler;
import org.apache.catalina.session.SessionService;
import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.response.HttpResponse;

public class LoginController extends AbstractSessionController {

    private static final String LOGIN_PATH = "/login";
    private static final String LOGIN_SUCCESS_PAGE = "/index.html";

    private static final String SESSION_KEY = "user";

    public LoginController() {
        initializeHandlers();
    }

    public LoginController(SessionService sessionService) {
        super(sessionService);
        initializeHandlers();
    }

    private void initializeHandlers() {
        List<Handler> handlers = List.of(
                Handler.ofGet(LOGIN_PATH, SafeExecutionWrapper.withExceptionHandling(this::doLoginGet)),
                Handler.ofPost(LOGIN_PATH, SafeExecutionWrapper.withExceptionHandling(this::doLoginPost))
        );
        registerHandlers(handlers);
    }

    private void doLoginPost(HttpRequest request, HttpResponse response) {
        LoginCredentials loginCredentials = createLoginCredentials(request);
        User user = getUser(loginCredentials);

        HttpSession session = getSession(request);
        session.setAttribute(SESSION_KEY, user);
        saveSession(session, response);
        response.redirectTo(LOGIN_SUCCESS_PAGE);
    }

    private LoginCredentials createLoginCredentials(HttpRequest request) {
        LoginRequestDto loginRequestDto = LoginRequestDto.of(request);
        return new LoginCredentials(loginRequestDto.account(), loginRequestDto.password());
    }

    private User getUser(LoginCredentials loginCredentials) {
        String account = loginCredentials.getAccount();
        User user = InMemoryUserRepository.findByAccount(account)
                .orElseThrow(() -> new UnauthorizedException("존재하지 않는 사용자입니다."));

        if (!user.checkPassword(loginCredentials)) {
            throw new UnauthorizedException("사용자 인증에 실패하였습니다.");
        }
        return user;
    }

    private void doLoginGet(HttpRequest request, HttpResponse response) {
        if (isLoginUser(request)) {
            response.redirectTo(LOGIN_SUCCESS_PAGE);
            return;
        }
        responseView(request, response);
    }

    private boolean isLoginUser(HttpRequest request) {
        HttpSession requestSession = getSession(request);
        return requestSession.getAttribute(SESSION_KEY) != null;
    }
}