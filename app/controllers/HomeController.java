package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
    @Inject
    WebJarAssets webJarAssets;

    public Result index() {
        return ok(index.render(webJarAssets));
    }

    public Result any(String any) {
        return ok(views.html.index.render(webJarAssets));
    }

}
