package com.group32.cpt202.LY_contributor.zyl_display;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class HeritageService {

    // 模拟数据（不用数据库也能跑）
    public List<Heritage> getAllHeritages() {
        List<Heritage> list = new ArrayList<>();

        Heritage h1 = new Heritage();
        h1.setId(1L);
        h1.setTitle("The Great Wall");
        h1.setDescription("One of the greatest wonders in the world");
        h1.setImageUrl("wall.jpg");
        list.add(h1);

        Heritage h2 = new Heritage();
        h2.setId(2L);
        h2.setTitle("Forbidden City");
        h2.setDescription("Ancient imperial palace");
        h2.setImageUrl("city.jpg");
        list.add(h2);

        return list;
    }

    // 根据ID查详情
    public Heritage getById(Long id) {
        List<Heritage> all = getAllHeritages();
        for (Heritage h : all) {
            if (h.getId().equals(id)) {
                return h;
            }
        }
        return null;
    }
}