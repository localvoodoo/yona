/**
 * Yobi, Project Hosting SW
 *
 * Copyright 2013 NAVER Corp.
 * http://yobi.io
 *
 * @Author Wansoon Park
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;
import java.util.Map;

import models.Project;
import models.PullRequest;
import models.User;
import models.enumeration.State;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;

public class PullRequestAppTest {
    protected FakeApplication app;

    private String ownerLoginId;
    private String projectName;
    private Long pullRequestNumber;

    @Test
    public void testCloseAnonymous() {
        initParameters("alecsiel", "sample", 10L);
        Result result = callAction(
                controllers.routes.ref.PullRequestApp.close(ownerLoginId, projectName, pullRequestNumber)
        );

        assertThat(status(result)).isEqualTo(SEE_OTHER);
    }

    @Test
    public void testCloseNotExistProject() {
        initParameters("alecsiel", "sample", 10L);
        User currentUser = User.findByLoginId("admin");

        Result result = callAction(
                controllers.routes.ref.PullRequestApp.close(ownerLoginId, projectName, pullRequestNumber),
                fakeRequest()
                .withSession(UserApp.SESSION_USERID, currentUser.id.toString())
              );

        assertThat(status(result)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void testCloseNotExistPullRequest() {
        initParameters("yobi", "projectYobi-1", 10L);
        User currentUser = User.findByLoginId("admin");

        Result result = callAction(
                controllers.routes.ref.PullRequestApp.close(ownerLoginId, projectName, pullRequestNumber),
                fakeRequest()
                .withSession(UserApp.SESSION_USERID, currentUser.id.toString())
              );

        assertThat(status(result)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void testClosePullRequest() {
        initParameters("yobi", "projectYobi", 1L);
        User currentUser = User.findByLoginId("admin");

        Result result = callAction(
                controllers.routes.ref.PullRequestApp.close(ownerLoginId, projectName, pullRequestNumber),
                fakeRequest()
                .withSession(UserApp.SESSION_USERID, currentUser.id.toString())
              );

        assertThat(status(result)).isEqualTo(SEE_OTHER);
        assertThat(PullRequest.findById(pullRequestNumber).state).isEqualTo(State.CLOSED);
    }

    @Test
    public void testClosePullRequestNotAllow() {
        initParameters("yobi", "projectYobi", 1L);
        User currentUser = User.findByLoginId("alecsiel");

        Result result = callAction(
                controllers.routes.ref.PullRequestApp.close(ownerLoginId, projectName, pullRequestNumber),
                fakeRequest()
                .withSession(UserApp.SESSION_USERID, currentUser.id.toString())
              );

        assertThat(status(result)).isEqualTo(FORBIDDEN);
        assertThat(PullRequest.findById(pullRequestNumber).state).isEqualTo(State.OPEN);
    }

    @Test
    public void testOpenAnonymous() {
        initParameters("alecsiel", "sample", 10L);
        Result result = callAction(
                controllers.routes.ref.PullRequestApp.open(ownerLoginId, projectName, pullRequestNumber)
        );

        assertThat(status(result)).isEqualTo(SEE_OTHER);
    }

    @Test
    public void testOpenPullRequestBadRequest() {
        initParameters("yobi", "projectYobi", 1L);
        User currentUser = User.findByLoginId("admin");

        Result result = callAction(
                controllers.routes.ref.PullRequestApp.open(ownerLoginId, projectName, pullRequestNumber),
                fakeRequest()
                .withSession(UserApp.SESSION_USERID, currentUser.id.toString())
              );

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void testOpenPullRequest() {
        initParameters("yobi", "HelloSocialApp", 1L);
        User currentUser = User.findByLoginId("admin");

        Result result = callAction(
                controllers.routes.ref.PullRequestApp.open(ownerLoginId, projectName, pullRequestNumber),
                fakeRequest()
                .withSession(UserApp.SESSION_USERID, currentUser.id.toString())
              );

        assertThat(status(result)).isEqualTo(SEE_OTHER);
        assertThat(PullRequest.findById(pullRequestNumber).state).isEqualTo(State.OPEN);
    }

    @Test
    public void testRejectAnonymous() {
        initParameters("alecsiel", "sample", 10L);
        Result result = callAction(
                controllers.routes.ref.PullRequestApp.reject(ownerLoginId, projectName, pullRequestNumber)
        );

        assertThat(status(result)).isEqualTo(SEE_OTHER);
    }

    @Test
    public void testRejectPullRequest() {
        initParameters("yobi", "projectYobi", 1L);
        User currentUser = User.findByLoginId("admin");

        Result result = callAction(
                controllers.routes.ref.PullRequestApp.reject(ownerLoginId, projectName, pullRequestNumber),
                fakeRequest()
                .withSession(UserApp.SESSION_USERID, currentUser.id.toString())
              );

        assertThat(status(result)).isEqualTo(SEE_OTHER);
        assertThat(PullRequest.findById(pullRequestNumber).state).isEqualTo(State.REJECTED);
    }

    @Test
    public void testAcceptAnonymous() {
        initParameters("alecsiel", "sample", 10L);
        Result result = callAction(
                controllers.routes.ref.PullRequestApp.accept(ownerLoginId, projectName, pullRequestNumber)
        );

        assertThat(status(result)).isEqualTo(SEE_OTHER);
    }

    @Test
    public void testCloseRoute() {
        initParameters("yobi", "projectYobi", 1L);
        String url = "/" + ownerLoginId + "/" + projectName + "/pullRequest/" + pullRequestNumber + "/close";
        User currentUser = User.findByLoginId("yobi");

        Result result = route(
            fakeRequest(POST, url).withSession(UserApp.SESSION_USERID, currentUser.id.toString())
        );
        assertThat(status(result)).isEqualTo(SEE_OTHER);

        Project project = Project.findByOwnerAndProjectName(ownerLoginId, projectName);
        PullRequest pullRequest = PullRequest.findOne(project, pullRequestNumber);

        assertThat(pullRequest.state).isEqualTo(State.CLOSED);
    }

    @Test
    public void testOpenRoute() {
        initParameters("yobi", "projectYobi", 1L);
        String url = "/" + ownerLoginId + "/" + projectName + "/pullRequest/" + pullRequestNumber + "/open";
        User currentUser = User.findByLoginId("yobi");

        Result result = route(
            fakeRequest(POST, url).withSession(UserApp.SESSION_USERID, currentUser.id.toString())
        );
        assertThat(status(result)).isEqualTo(BAD_REQUEST);

        Project project = Project.findByOwnerAndProjectName(ownerLoginId, projectName);
        PullRequest pullRequest = PullRequest.findOne(project, pullRequestNumber);

        assertThat(pullRequest.state).isEqualTo(State.OPEN);
    }

    @Test
    public void testRejectRoute() {
        initParameters("yobi", "projectYobi", 1L);
        String url = "/" + ownerLoginId + "/" + projectName + "/pullRequest/" + pullRequestNumber + "/reject";
        User currentUser = User.findByLoginId("yobi");

        Result result = route(
            fakeRequest(POST, url).withSession(UserApp.SESSION_USERID, currentUser.id.toString())
        );
        assertThat(status(result)).isEqualTo(SEE_OTHER);

        Project project = Project.findByOwnerAndProjectName(ownerLoginId, projectName);
        PullRequest pullRequest = PullRequest.findOne(project, pullRequestNumber);

        assertThat(pullRequest.state).isEqualTo(State.REJECTED);
    }

    @BeforeClass
    public static void beforeClass() {
        callAction(
                routes.ref.Application.init()
        );
    }

    @Before
    public void before() {
        Map<String, String> config = support.Config.makeTestConfig();
        app = Helpers.fakeApplication(config);
        Helpers.start(app);
    }

    @After
    public void after() {
        Helpers.stop(app);
    }

    private void initParameters(String ownerLoginId, String projectName, Long pullRequestNumber) {
        this.ownerLoginId = ownerLoginId;
        this.projectName = projectName;
        this.pullRequestNumber = pullRequestNumber;
    }
}
