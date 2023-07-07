package com.zhifu.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    //日志打印
    private static final Logger logger =  LoggerFactory.getLogger(SensitiveFilter.class);
    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    //@PostConstruct注解的方法在项目启动的时候执行这个方法，也可以理解为在spring容器启动的时候执行，可作为一些数据的常规化加载，比如数据字典之类的。
    //如果想在生成对象时完成某些初始化操作，而偏偏这些初始化操作又依赖于依赖注入，那么就无法在构造函数中实现。
    //为此，可以使用@PostConstruct注解一个方法来完成初始化，@PostConstruct注解的方法将会在依赖注入完成后被自动调用。
    //Constructor >> @Autowired >> @PostConstruct
    @PostConstruct
    //该注解表示这是一个初始化方法，当容器实例化该bean以后，在调用它的构造器后，该方法就会被自动调用。因此，我们拿来用的时候，就是已经初始化好的前缀树了
    public void init(){
        //读取敏感词配置文件
        //通过读取类加载文件（编译项目后产生的文件）中的指定文件来实现
        //获取的是输入流
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ){
            String keyword;
            while((keyword = reader.readLine()) != null){
                //添加到前缀树中
                addKeyword(keyword);
            }

        }catch(IOException e){
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    //将一个敏感词添加到前缀树中去
    private void addKeyword(String keyword) {
        //初始化生成 针对前缀树的指针 tempNode 指向前缀树的根节点
        TrieNode tempNode = rootNode;

        //开始对字符串的遍历
        for(int i=0; i < keyword.length(); i++){
            char c = keyword.charAt(i);

            //初始化对应于当前字符 c 的子节点 subTrieNode
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode == null){
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //更新前缀树的指针
            tempNode = subNode;

            //最重要一步，设置结束标识
            if(i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }

    }

    /**
     * 过滤敏感词
     *
     * @param text  待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text ){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //输出字符串临时结果
        StringBuilder sb = new StringBuilder();
        while(begin < text.length()){
            char c = text.charAt(position);
            //跳过特殊符号
            if(isSymbol(c)){
                //如果指针1处于根节点，将此符号计入结果中，并让指针2向下走一步
                if(tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                //避免最后一个字符是特殊符号，最后一个字符是特殊符号，说明当前begin的都到最后了都不是敏感词，自然就begin+1啦，
                // 还可以避免position越界！！！！
                if(position == text.length() - 1){
                    position = ++begin;
                    continue;
                }
                //无论符号在开头还是在中间，指针3都需要跳过它，并向下走一步
                position++;
                continue;//跳过特殊字符后，直接进入下一轮循环
            }

            //走到这一步说明，当前字符不是特殊符号，需要开始检查节点字符
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                //以begin为开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一位置
                position = ++begin;
                //前缀树指针重新指向根节点
                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd()){
                //发现敏感词，将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一位置
                begin = ++position;
                //前缀树指针重新指向根节点
                tempNode = rootNode;
            }else{
                //此时，说明最后这以begin为开头的字符串有嫌疑，

                //当此时position已经是最后一位的时候，说明最后这以begin为开头的字符串有嫌疑，但已经可以断定不是敏感词，
                //因此，需要将begin加入到结果中，并同时更新begin和position
                if(position == text.length() - 1){
                    sb.append(text.charAt(begin));
                    //进入下一位置
                    position = ++begin;
                    //前缀树指针重新指向根节点
                    tempNode = rootNode;
                }

                //此种情况是指position还没到最后一位，还在可以增加的范围之内
                //因此，需要做下一个字符的检查,只需要做position的更新就行，begin不用更新
                position++;

            }

        }
        return sb.toString();


    }

    //判断是否为特殊符号
    private boolean isSymbol(char c){
        //CharUtils.isAsciiAlphanumeric(c)：该函数用来判断c是否为合法普通字符，例如123abc等，若是，返回true，否则返回false
        //0x2E80 ~ 0x9FFF 是东亚文字范围
        //返回值的意思就是，不是123abc 并且 不是东亚字符 的字符，就会返回true
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    //前缀树定义
    private class TrieNode{
        //关键词结束标识
        private boolean isKeywordEnd = false;

        //当前节点的子节点(key是下级字符，value是下级节点)
        private Map<Character,TrieNode>  subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node ){
            subNodes.put(c,node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
