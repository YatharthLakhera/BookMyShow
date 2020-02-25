package com.project.bookmyshow.db.dao;

import com.project.bookmyshow.db.ConnectionFactory;
import com.project.bookmyshow.db.mappers.Show;
import com.project.bookmyshow.db.mappers.ShowDynamicSqlSupport;
import com.project.bookmyshow.db.mappers.ShowMapper;
import com.project.bookmyshow.enums.ShowType;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Repository
public class ShowDAO {

    public Show getById(int showId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        ShowMapper showMapper = sqlSession.getMapper(ShowMapper.class);
        return showMapper.selectByPrimaryKey(showId);
    }

    public List<Show> getAllShow() {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        ShowMapper showMapper = sqlSession.getMapper(ShowMapper.class);
        return showMapper.selectByExample().build().execute();
    }

    public Show insertOrUpdateShow(ShowType showType, String showName) {
        Show show = getShowByName(showName, showType);
        if (show == null) {
            show = new Show();
            show.setShowName(showName);
            show.setShowType(showType);
            @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
            ShowMapper showMapper = sqlSession.getMapper(ShowMapper.class);
            int entries = showMapper.insertSelective(show);
            sqlSession.commit();
            log.info("Show inserted : {}",entries);
        }
        return show;
    }

    public Show getShowByName(String showName, ShowType showType) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        ShowMapper showMapper = sqlSession.getMapper(ShowMapper.class);
        ShowDynamicSqlSupport.Show showSqlSupport = new ShowDynamicSqlSupport.Show();
        List<Show> showList = showMapper.selectByExample()
                .where(showSqlSupport.showName, SqlBuilder.isEqualTo(showName))
                .and(showSqlSupport.showType, SqlBuilder.isEqualTo(showType))
                .limit(1).build().execute();
        Show resultShow = null;
        if (!CollectionUtils.isEmpty(showList)) {
            resultShow = showList.get(0);
        }
        return resultShow;
    }
}
