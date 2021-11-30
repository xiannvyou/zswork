package life.zswork.reptlie.httpclient;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import life.zswork.reptlie.ReptileException;
import life.zswork.reptlie.dto.CommentDetailDTO;
import life.zswork.reptlie.dto.WeiBoUserDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.core.util.CollectionUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 微博客户端
 */
@Component
@Lazy
@Slf4j
public class WeiBoClient {

    private OkHttpClient client;

    private final static String url = "https://s.weibo.com/weibo?q=%s&page=%s";
    private final static String commentUrl = "https://weibo.com/ajax/statuses/buildComments?id=%s&is_show_bulletin=2&count=200";

    @PostConstruct
    private void init() {
        //链接池
        ConnectionPool connectionPool = new ConnectionPool(5, 10, TimeUnit.SECONDS);
        //事件监听器  new EventListener();
        client = new OkHttpClient().newBuilder()
                .connectionPool(connectionPool)
                //创建链接的时间
                .connectTimeout(5, TimeUnit.SECONDS)
                //读
                .readTimeout(30, TimeUnit.SECONDS)
                //写
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 获取话题ID
     */
    public List<Long> getTopicId(String topic, int page, String cookie) {
        if (StringUtils.isEmpty(topic)) {
            Lists.newArrayList();
        }
        Request request = new Request.Builder().url(String.format(url, topic, page)).get()
                .header("Cookie", cookie)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String html = response.body().string();
                Document document = Jsoup.parse(html);
                Elements elements = document.getElementsByClass("card-wrap");
                if (CollectionUtils.isEmpty(elements)) {
                    throw new ReptileException("请登录");
                }
                List<Long> mid = elements.stream().reduce(Lists.newArrayList(), (l, e) -> {
                    String str = e.attr("mid");
                    try {
                        l.add(Long.parseLong(str));
                    } catch (NumberFormatException ex) {
                    }
                    return l;
                }, (l, e) -> l);
                return mid.stream().distinct().collect(Collectors.toList());
            }
        } catch (IOException e) {
        }
        return Lists.newArrayList();
    }

    public List<WeiBoUserDTO> getUserDetail(Long txtId) {
        try {
            Response response = client.newCall(new Request.Builder().url(String.format(commentUrl, txtId)).build()).execute();
            if (response.isSuccessful()) {
                CommentDetailDTO commentDetailDTO = JSON.parseObject(response.body().string(), CommentDetailDTO.class);
                return commentDetailDTO.getData().stream().map(detail -> WeiBoUserDTO.builder()
                        .userId(detail.getUser().getId())
                        .userName(detail.getUser().getName())
                        .location(detail.getUser().getLocation())
                        .txt(detail.getText())
                        .build()).collect(Collectors.toList());
            }
        } catch (IOException e) {
        }
        return Lists.newArrayList();
    }

    @PreDestroy
    private void destroy() {
        if (Objects.nonNull(client)) {
            //线程池关闭
            client.dispatcher().executorService().shutdownNow();
            //清除缓存
            client.connectionPool().evictAll();
        }
    }
}
