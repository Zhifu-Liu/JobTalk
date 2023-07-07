package com.zhifu.community.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

//根据目前测试的结果，可以看出在当前ES版本及对应Spring依赖包下，RestHighLevelClient有默认的配置,如下行
//hostName":"localhost","port":9200,"schemeName":"http
//因此，本文件的配置类通常是不需要的

//但是，在ES服务器有特殊改动（如默认端口做了更改）或对连接ES服务器有特定要求（如使用ssl，设置报头，设置路径前缀等）的情况下，
// 需要配置类配合，对RestHighLevelClient客户端作自定义配置

@Configuration
public class ESRestClientConfig extends AbstractElasticsearchConfiguration {

    //localhost:9200 写在配置文件中就可以了
    //通过@Value注解来将配置注入给String ESUrl
    @Value("${elasticSearch.url}")
    private String ESUrl;

 /*@Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("127.0.0.1", 9200,  "http"))) ;
        return client;
    }*/


@Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(ESUrl)
                /*.connectedTo("localhost:9200", "localhost:9291")
                .useSsl()
                .withProxy("localhost:8888")
                .withPathPrefix("ela")
                .withConnectTimeout(Duration.ofSeconds(5))
                .withSocketTimeout(Duration.ofSeconds(3))
                .withDefaultHeaders(defaultHeaders)
                .withBasicAuth(username, password)
                .withHeaders(() -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    return headers;
                })
                . // ... other options*/
                .build();
        return RestClients.create(clientConfiguration).rest();
    }

}
