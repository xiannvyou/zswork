package life.zswork.reptlie.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.google.common.base.Objects;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WeiBoUserDTO {

    @ExcelProperty(value = "用户ID", order = 1)
    @ColumnWidth(25)
    private Long userId;

    @ExcelProperty(value = "用户名称", order = 2)
    @ColumnWidth(30)
    public String userName;

    @ExcelProperty(value = "注册地", order = 3)
    @ColumnWidth(15)
    public String location;

    @ExcelProperty(value = "评论", order = 4)
    public String txt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeiBoUserDTO that = (WeiBoUserDTO) o;
        return Objects.equal(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
}
