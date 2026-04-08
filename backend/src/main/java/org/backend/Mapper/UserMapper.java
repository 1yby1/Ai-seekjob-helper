package org.backend.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.backend.Model.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
