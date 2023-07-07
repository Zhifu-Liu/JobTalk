package com.zhifu.community.dao.elasticsearch;

import com.zhifu.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

//ES可以看作是一种特殊的数据库，则DiscussPostRepository则是数据访问层的接口
//此处不能使用@Mapper注解，因为其是MyBatis的专有注解
//因此，此处应使用@Repository注解,其是Spring提供的数据访问层的接口
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {
    //该数据访问层接口无需声明任何方法，只需继承spring对ES的默认接口即可，默认接口已经实现了增删改查等方法
    //默认接口需要声明泛型，用以规范该接口处理的实体类型和主键类型
}
