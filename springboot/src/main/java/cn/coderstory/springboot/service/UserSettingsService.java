package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.UserSettings;
import cn.coderstory.springboot.mapper.UserSettingsMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSettingsService {
    
    private final UserSettingsMapper userSettingsMapper;
    
    public String getTheme(Long userId) {
        UserSettings settings = userSettingsMapper.selectOne(
            new QueryWrapper<UserSettings>()
                .eq("user_id", userId)
        );
        return settings != null ? settings.getTheme() : "light";
    }
    
    public void saveTheme(Long userId, String theme) {
        UserSettings settings = userSettingsMapper.selectOne(
            new QueryWrapper<UserSettings>()
                .eq("user_id", userId)
        );
        
        if (settings == null) {
            settings = new UserSettings();
            settings.setUserId(userId);
            settings.setTheme(theme);
            userSettingsMapper.insert(settings);
        } else {
            settings.setTheme(theme);
            userSettingsMapper.updateById(settings);
        }
    }
}
