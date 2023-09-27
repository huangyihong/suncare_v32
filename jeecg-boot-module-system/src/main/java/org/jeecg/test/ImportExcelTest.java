package org.jeecg.test;

import com.ai.common.utils.ExcelXUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Auther: zhangpeng
 * @Date: 2021/3/29 16
 * @Description:
 */
public class ImportExcelTest {
    public static void main(String[] args) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        FileInputStream im = new FileInputStream("D:\\下载\\超临床适应症_导出_20210329035251.xlsx");
        List<List<String>> list = ExcelXUtils.readSheet(0, 1, im);
        list.addAll(list);
        list.addAll(list);
        list.addAll(list);
        list.addAll(list);
        list.addAll(list);
        list.addAll(list);
        list.addAll(list);
        list.addAll(list);

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        long seconds = duration.toMillis() / 1000;//相差毫秒数
        System.out.println("读取时长： "+ seconds +"秒 。");

        startTime = LocalDateTime.now();


        Map<String, List<List<String>>> map = list.stream()
                .collect(Collectors.groupingBy(r -> r.get(2) + "::::" +  r.get(5)));


        endTime = LocalDateTime.now();
        duration = Duration.between(startTime, endTime);
        seconds = duration.toMillis() / 1000;//相差毫秒数
        System.out.println("运行时长： "+ seconds +"秒 。");

        System.out.println("总数：" + list.size() + "，分组数:" + map.size());

    }
}
