package life.zswork.reptlie;

import life.zswork.reptlie.dto.WeiBoUserDTO;
import life.zswork.reptlie.httpclient.WeiBoClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class BaseTest {

    @Resource
    private WeiBoClient client;

    @Test
    public void test() {
        Stream.iterate(1, o -> o + 1).limit(100).collect(Collectors.toList()).stream().parallel().forEach(
                id -> {
                    Stream.iterate(1, o -> o + 1).limit(1000).collect(Collectors.toList()).stream().parallel().forEach(id1 -> {
                        log.info("{}",Thread.currentThread().getClass().getName());
                        log.info("{}:{}", id, id1);
                    });
                }
        );
    }
}
