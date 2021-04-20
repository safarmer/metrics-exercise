package co.thatch.example.metrics;

import java.util.Random;
import java.util.logging.Logger;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;

@Controller("/foo")
public class FooController {

    private static final Logger logger = Logger.getLogger(FooController.class.getName());

    @Get
    public String getFoo() {
        return "foo";
    }

    @Post
    public String newFoo() {
        logger.info("Creating a new FOO");
        final var rand = new Random();
        final var result = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            char next = (char) (rand.nextInt('z' - 'A') + 'A');
            result.append(next);
        }
        logger.info("New FOO: %s".formatted(result));
        return result.toString();
    }
}
