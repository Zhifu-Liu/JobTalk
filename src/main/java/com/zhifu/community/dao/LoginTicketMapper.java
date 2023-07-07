package com.zhifu.community.dao;

import com.zhifu.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper//表示这是一个数据访问对象，需要Spring容器管理
@Deprecated//该注释表明如下接口或类已被废弃，不推荐使用
public interface LoginTicketMapper {
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            "<script>",//script标签表示这是脚本，在此标签内就可以加if标签
            "update login_ticket set status=#{status} where ticket = #{ticket} ",
            "<if test=\"ticket!=null\">",
            "and 1=1",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);
}
