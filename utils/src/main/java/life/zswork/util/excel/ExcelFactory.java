package life.zswork.util.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static void main(String[] args) {
        List<Integer> collect = Stream.iterate(0, o -> o + 1).limit(1000).collect(Collectors.toList());
        List<Integer> collect1 = collect.stream().parallel().collect(Collectors.toList());
        System.out.println(collect1.size());
    }
}
