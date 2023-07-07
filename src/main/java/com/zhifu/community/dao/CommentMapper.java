package com.zhifu.community.dao;

import com.zhifu.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    //查询一定量的符合条件的评论
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    //查询评论的条目数量
    int selectCountByEntity(int entityType, int entityId);

    //增加评论
    int insertComment(Comment comment);

    //根据ID查询一个评论
    Comment selectCommentById(int id);
}
