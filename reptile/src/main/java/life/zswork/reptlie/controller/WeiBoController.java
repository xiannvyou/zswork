package life.zswork.reptlie.controller;

import com.alibaba.excel.util.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import life.zswork.reptlie.ReptileException;
import life.zswork.reptlie.dto.WeiBoUserDTO;
import life.zswork.reptlie.httpclient.WeiBoClient;
import life.zswork.util.dac.SumFuture;
import life.zswork.util.excel.ExcelFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("weibo")
@Slf4j
public class WeiBoController {

    @Resource
    private HttpServletRequest request;

    @Resource
    private WeiBoClient client;

    @GetMapping("grab/user")
    public ResponseEntity<String> grabUser(@RequestParam("key") String key,
                                           @RequestParam("maxPage") Integer maxPage) {
        if (StringUtils.isEmpty(key) || Objects.isNull(maxPage)) {
            throw new ReptileException("参数异常");
        }
        String cookie = request.getHeader("cookie");
        List<Long> topicId = client.getTopicId(key, 1, request.getHeader("cookie"));
        List<WeiBoUserDTO> weiBoUserDTOList = SumFuture.single(Stream.iterate(1, o -> o + 1).limit(maxPage).collect(Collectors.toList()))
                .parallelReduce(Lists.newCopyOnWriteArrayList(), (l, p) -> {
                    List<Long> list = client.getTopicId(key, p, cookie);
                    SumFuture.single(list).parallelForeach(id -> {
                        List<WeiBoUserDTO> userDetailList = client.getUserDetail(id);
                        if (CollectionUtils.isNotEmpty(userDetailList)) {
                            l.addAll(userDetailList);
                        }
                    });
                    return l;
                }, (u, p) -> u);
        log.info("{}",weiBoUserDTOList);
        ByteArrayOutputStream excel = ExcelFactory.createExcel(weiBoUserDTOList.stream().distinct().collect(Collectors.toList()), Sets.newHashSet());
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("/Users/z_shuai/Documents/project/1.xlsx");
            fileOutputStream.write(excel.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(400).body("fail");
        }
        return ResponseEntity.ok("success");
    }

}
