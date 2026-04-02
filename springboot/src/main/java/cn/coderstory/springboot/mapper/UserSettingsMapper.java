package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.UserSettings;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSettingsMapper extends BaseMapper<UserSettings> {
}
