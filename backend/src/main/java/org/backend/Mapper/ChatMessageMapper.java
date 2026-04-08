package org.backend.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.backend.Model.ChatMessage;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {


}
