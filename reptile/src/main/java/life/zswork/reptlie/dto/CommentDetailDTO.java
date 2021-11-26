package life.zswork.reptlie.dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class CommentDetailDTO {
    private Integer ok;
    public List<Detail> data;

    @Data
    public static class Detail{
        private String text;
        private User user;
    }

    @Data
    public static class User{
        private Long id;
        private String name;
        private String location;
    }
}
