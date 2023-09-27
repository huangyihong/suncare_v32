package org.jeecg.common.system.util;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: zhangpeng
 * @Date: 2021/8/2 17
 * @Description:
 */
public class PdfReaderUtil {

    public static List<KeywordInfo> readKeyword(String path, String keywords) throws IOException {
        List<KeywordInfo> list = new ArrayList<>();
        PDDocument document = PDDocument.load(new File(path));
        int wordLen = keywords.length();

        // 获取页码
        int pages = document.getNumberOfPages();
        // 读文本内容
        PDFTextStripper stripper = new PDFTextStripper();
        for(int i = 0; i < pages; i++){
            int pageNo = i + 1;
            stripper.setStartPage(pageNo);
            stripper.setEndPage(pageNo);
            String content = stripper.getText(document);
            List<Integer> indexList = new ArrayList<>();
            int splitIndex, lastIndex = -1;
            while((splitIndex = content.indexOf(keywords, lastIndex + 1)) > -1){
                indexList.add(splitIndex);
                lastIndex = splitIndex + wordLen;
            }
            if(indexList.size() > 0){
                KeywordInfo lastInfo = null;
                for(int index: indexList){
                    int startIndex = content.lastIndexOf("\n", index) + 1;

                    if(lastInfo != null && lastInfo.getParagraphStart() == startIndex){
                        continue;
                    }
                    int endIndex = content.indexOf("\n", index + wordLen);
                    if(endIndex == -1){
                        endIndex = content.length();
                    }

                    String paragraphText = content.substring(startIndex, endIndex);

                    KeywordInfo keywordInfo = new KeywordInfo();
                    keywordInfo.setPageNo(i);
                    keywordInfo.setParagraphStart(startIndex);
                    keywordInfo.setParagraphEnd(endIndex);
                    keywordInfo.setParagraph(paragraphText);
                    list.add(keywordInfo);
                    lastInfo = keywordInfo;
                }
            }
        }

        return list;

    }
    

    public static List<KeywordInfo> readAllParagraph(String path) throws IOException {
        List<KeywordInfo> list = new ArrayList<>();
        PDDocument document = PDDocument.load(new File(path));

        // 获取页码
        int pages = document.getNumberOfPages();
        // 读文本内容
        PDFTextStripper stripper = new PDFTextStripper();
        for(int i = 0; i < pages; i++){
            int pageNo = i + 1;
            stripper.setStartPage(pageNo);
            stripper.setEndPage(pageNo);
            String content = stripper.getText(document);

            List<KeywordInfo> paragraphList = Arrays.stream(content.split("\n")).filter(StringUtils::isNotBlank).map(r -> {
                KeywordInfo keywordInfo = new KeywordInfo();
                keywordInfo.setPageNo(pageNo - 1);
                keywordInfo.setParagraph(r);
                return keywordInfo;
            }).collect(Collectors.toList());

            list.addAll(paragraphList);
        }

        return list;

    }


    @Data
    public static class KeywordInfo {
        private Integer pageNo;
        private String paragraph;
        private Integer paragraphStart;
        private Integer paragraphEnd;
    }

    public static void main(String[] args)  throws IOException {

        String filepath = "E:\\ASIAProject\\suncare_v3\\upFiles\\files\\20200410\\A15001_初治菌测试_15864906354162.pdf";

        List<KeywordInfo> list = readKeyword(filepath, "检查");
        System.out.println(list.size());

    }
}
