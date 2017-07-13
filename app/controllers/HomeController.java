package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import javax.inject.Inject;

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
