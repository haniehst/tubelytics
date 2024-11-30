package controllers;

import org.junit.Before;
import org.junit.Test;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link HomeController} class.
 * <p>
 * This test class verifies the behavior of methods in {@code HomeController},
 * such as ensuring the correct return types and verifying non-null responses.
 * </p>
 *
 * @author Hanieh
 */
public class HomeControllerTest {

    private HomeController homeController;

    /**
     * Set up the {@code HomeController} instance before each test.
     */
    @Before
    public void setUp() {
        // Initialize the HomeController
        homeController = new HomeController();
    }

    /**
     * Test to verify that the {@code index()} method returns a non-null {@link CompletionStage}
     * containing a {@link Result}.
     */
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

    /**
     * Test to verify that the {@code websocketPage()} method returns a non-null {@link CompletionStage}
     * containing a {@link Result}.
     */
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
