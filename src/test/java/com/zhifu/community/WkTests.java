package com.zhifu.community;

import java.io.IOException;

public class WkTests {
    public static void main(String[] args) {
        String cmd = "d:/Program Files/wkhtmltopdf/bin/wkhtmltopdf https://javaguide.cn d:/ProgramData/MyProject/data/wk-pdfs/javaguide1.pdf";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("OK!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
