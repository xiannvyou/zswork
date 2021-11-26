package life.zswork.reptlie;

import life.zswork.reptlie.dto.WeiBoUserDTO;
import life.zswork.reptlie.httpclient.WeiBoClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseTest {

    @Resource
    private WeiBoClient client;
    @Test
    public void test(){
        //List<Long> list = client.getTopicId("篮球", 1,"");
        List<WeiBoUserDTO> userDetail = client.getUserDetail(4706806699658489L);
        System.out.println(userDetail);
    }
}
