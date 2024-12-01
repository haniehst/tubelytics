package controllers;

import org.junit.Before;
import org.junit.Test;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link HomeController}.
 * <p>
 * These tests validate that the {@code HomeController} methods return the expected results
 * and handle CompletionStage<Result> correctly.
 * </p>
 *
 * @author Hanieh
 */
public class HomeControllerTest {

    private HomeController homeController;

    /**
     * Sets up the test environment by initializing the {@link HomeController}.
     */
    @Before
    public void setUp() {
        homeController = new HomeController();
    }

    /**
     * Tests the {@link HomeController#index()} method to ensure it returns
     * a non-null {@link CompletionStage} containing a {@link Result}.
     */
    @Test
    public void testIndexReturnType() {
        CompletionStage<Result> result = homeController.index();

        assertNotNull("The index() method should return a non-null CompletionStage", result);

        result.toCompletableFuture().thenAccept(res -> {
            assertNotNull("The CompletionStage should contain a non-null Result", res);
            assertTrue("The Result should be an instance of play.mvc.Result", res instanceof Result);
        });
    }

    /**
     * Tests the {@link HomeController#websocketPage()} method to ensure it returns
     * a non-null {@link CompletionStage} containing a {@link Result}.
     */
    @Test
    public void testWebsocketPageReturnType() {
        CompletionStage<Result> result = homeController.websocketPage();

        assertNotNull("The websocketPage() method should return a non-null CompletionStage", result);

        result.toCompletableFuture().thenAccept(res -> {
            assertNotNull("The CompletionStage should contain a non-null Result", res);
            assertTrue("The Result should be an instance of play.mvc.Result", res instanceof Result);
        });
    }
}
