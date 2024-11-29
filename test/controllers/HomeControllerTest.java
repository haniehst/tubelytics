//package controllers;
//
//import org.junit.Test;
//import play.Application;
//import play.inject.guice.GuiceApplicationBuilder;
//import play.mvc.Http;
//import play.mvc.Result;
//import play.test.WithApplication;
//
//import static org.junit.Assert.assertEquals;
//import static play.mvc.Http.Status.OK;
//import static play.test.Helpers.GET;
//import static play.test.Helpers.route;
//
//public class HomeControllerTest extends WithApplication {
//
//    @Override
//    protected Application provideApplication() {
//        return new GuiceApplicationBuilder().build();
//    }
//
//    @Test
//    public void testIndex() {
//        Http.RequestBuilder request = new Http.RequestBuilder()
//                .method(GET)
//                .uri("/");
//
//        Result result = route(app, request);
//        assertEquals(OK, result.status());
//    }
//
//}
package controllers;

import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;

/**
 * Unit tests for HomeController to validate its endpoints.
 *
 * @author Adriana
 * @author Hanieh
 */
public class HomeControllerTest extends WithApplication {

    /**
     * Provides the Play application for testing.
     *
     * @return an instance of Application.
     */
    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }

    /**
     * Test to ensure the index endpoint responds with an OK status.
     */
    @Test
    public void testIndex() {
        // Simulate a GET request to the root endpoint ("/")
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");

        // Route the request through the Play application
        Result result = route(app, request);

        // Assert that the response status is OK
        assertEquals(OK, result.status());
    }

    /**
     * Test to ensure the WebSocket page endpoint responds with an OK status.
     */
    @Test
    public void testWebSocketPage() {
        // Simulate a GET request to the WebSocket page endpoint ("/websocket")
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/websocket");

        // Route the request through the Play application
        Result result = route(app, request);

        // Assert that the response status is OK
        assertEquals(OK, result.status());
    }
}


