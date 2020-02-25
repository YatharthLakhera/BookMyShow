package com.project.bookmyshow.db.dao;

import com.project.bookmyshow.db.ConnectionFactory;
import com.project.bookmyshow.db.mappers.LiveShow;
import com.project.bookmyshow.db.mappers.LiveShowDynamicSqlSupport;
import com.project.bookmyshow.db.mappers.LiveShowMapper;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Repository
public class LiveShowDAO {

    public LiveShow getById(int liveShowId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        LiveShowMapper liveShowMapper = sqlSession.getMapper(LiveShowMapper.class);
        return liveShowMapper.selectByPrimaryKey(liveShowId);
    }

    public List<LiveShow> getAllLiveShow() {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        LiveShowMapper liveShowMapper = sqlSession.getMapper(LiveShowMapper.class);
        return liveShowMapper.selectByExample().build().execute();
    }

    public List<LiveShow> getAllLiveShowBy(int showId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        LiveShowMapper liveShowMapper = sqlSession.getMapper(LiveShowMapper.class);
        LiveShowDynamicSqlSupport.LiveShow liveShowSqlSupport = new LiveShowDynamicSqlSupport.LiveShow();
        return liveShowMapper.selectByExample()
                .where(liveShowSqlSupport.showId, SqlBuilder.isEqualTo(showId))
                .build().execute();
    }

    public boolean isShowLive(int showId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        LiveShowMapper liveShowMapper = sqlSession.getMapper(LiveShowMapper.class);
        LiveShowDynamicSqlSupport.LiveShow liveShowSqlSupport = new LiveShowDynamicSqlSupport.LiveShow();
        List<LiveShow> liveShowList = liveShowMapper.selectByExample()
                .where(liveShowSqlSupport.showId, SqlBuilder.isEqualTo(showId))
                .build().execute();
        boolean isShowLive = true;
        if (CollectionUtils.isEmpty(liveShowList)) {
            isShowLive = false;
        }
        log.info("isShowLive : {}", isShowLive);
        return isShowLive;
    }

    public LiveShow insertOrUpdateLiveShow(int showId, int hallId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        LiveShowMapper liveShowMapper = sqlSession.getMapper(LiveShowMapper.class);
        LiveShow liveShow = getLiveShowBy(showId, hallId, sqlSession);
        if (liveShow == null) {
            liveShow = new LiveShow();
            liveShow.setHallId(hallId);
            liveShow.setShowId(showId);
            int entries = liveShowMapper.insertSelective(liveShow);
            sqlSession.commit();
            log.info("LiveShow Inserted : {}", entries);
        }
        return liveShow;
    }

    public LiveShow getLiveShowBy(int showId, int hallId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        return getLiveShowBy(showId, hallId, sqlSession);
    }

    public LiveShow getLiveShowBy(int showId, int hallId, SqlSession sqlSession) {
        LiveShowMapper liveShowMapper = sqlSession.getMapper(LiveShowMapper.class);
        LiveShowDynamicSqlSupport.LiveShow liveShowSqlSupport = new LiveShowDynamicSqlSupport.LiveShow();
        List<LiveShow> liveShowList = liveShowMapper.selectByExample()
                .where(liveShowSqlSupport.showId, SqlBuilder.isEqualTo(showId))
                .and(liveShowSqlSupport.hallId, SqlBuilder.isEqualTo(hallId))
                .limit(1).build().execute();
        LiveShow liveShow = null;
        if (!CollectionUtils.isEmpty(liveShowList)) {
            liveShow = liveShowList.get(0);
        }
        return liveShow;
    }

    public List<LiveShow> getLiveShowByHallId(int hallId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        return getLiveShowByHallId(hallId, sqlSession);
    }

    public List<LiveShow> getLiveShowByHallId(int hallId, SqlSession sqlSession) {
        LiveShowMapper liveShowMapper = sqlSession.getMapper(LiveShowMapper.class);
        LiveShowDynamicSqlSupport.LiveShow liveShowSqlSupport = new LiveShowDynamicSqlSupport.LiveShow();
        return liveShowMapper.selectByExample()
                .where(liveShowSqlSupport.hallId, SqlBuilder.isEqualTo(hallId))
                .limit(1).build().execute();
    }
}
