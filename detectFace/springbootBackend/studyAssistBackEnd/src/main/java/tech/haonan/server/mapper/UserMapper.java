package tech.haonan.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import tech.haonan.server.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
