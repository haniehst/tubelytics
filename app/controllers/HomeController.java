package controllers;

import play.mvc.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import views.html.*;

/**
 * HomeController is responsible for handling HTTP requests to the application's home page.
 *
 * <p>This controller provides a method to render the home page of the application asynchronously.</p>
 *
 * @author Hanieh
 */
public class HomeController extends Controller {

    /**
     * Renders the home page of the application asynchronously.
     *
     * <p>This method returns a CompletionStage containing the HTTP  Result
     * of rendering the home page. The result is immediately completed with the HTML content
     * of the index view.</p>
     *
     * @return a CompletionStage that, when completed, will contain an HTTP Result
     *         representing the rendered home page
     */
    public CompletionStage<Result> index() {
        return CompletableFuture.completedFuture(ok(index.render()));
    }
}
