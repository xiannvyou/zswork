package life.zswork.util.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.alibaba.excel.support.ExcelTypeEnum.XLSX;

@Slf4j
public final class ExcelFactory {

    public static <T> ByteArrayOutputStream createExcel(List<T> dataList, Set<String> needFiledNames) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EasyExcel.write(outputStream, dataList.get(0).getClass())
                .excelType(XLSX)
                .includeColumnFiledNames(needFiledNames).sheet().doWrite(dataList);
        return outputStream;
    }

    /**
     * 调用easyExcel 读取excel文件中的内容
     */
    public static <T> List<T> readExcel(InputStream inputStream, Class<T> tClass) {
        ExcelReaderBuilder head = EasyExcel.read(inputStream).head(tClass);
        return head.sheet(0).doReadSync();
    }
}
