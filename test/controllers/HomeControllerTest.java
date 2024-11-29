package controllers;

import org.junit.Before;
import org.junit.Test;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HomeControllerTest {

    private HomeController homeController;

    @Before
    public void setUp() {
        // Initialize the HomeController
        homeController = new HomeController();
    }

    @Test
    public void testIndexReturnType() {
        // Call the index() method
        CompletionStage<Result> result = homeController.index();

        // Assert that the result is not null
        assertNotNull("The index() method should return a non-null CompletionStage", result);

        // Assert that the result contains a Play Result
        result.toCompletableFuture().thenAccept(res -> {
            assertNotNull("The CompletionStage should contain a non-null Result", res);
            assertTrue("The Result should be an instance of play.mvc.Result", res instanceof Result);
        });
    }

    @Test
    public void testWebsocketPageReturnType() {
        // Call the websocketPage() method
        CompletionStage<Result> result = homeController.websocketPage();

        // Assert that the result is not null
        assertNotNull("The websocketPage() method should return a non-null CompletionStage", result);

        // Assert that the result contains a Play Result
        result.toCompletableFuture().thenAccept(res -> {
            assertNotNull("The CompletionStage should contain a non-null Result", res);
            assertTrue("The Result should be an instance of play.mvc.Result", res instanceof Result);
        });
    }
}
