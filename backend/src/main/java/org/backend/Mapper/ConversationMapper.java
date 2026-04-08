package org.backend.Mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.backend.Model.Conversation;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
