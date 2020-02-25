package com.project.bookmyshow.db.dao;

import com.project.bookmyshow.db.ConnectionFactory;
import com.project.bookmyshow.db.mappers.ScheduledLiveShow;
import com.project.bookmyshow.db.mappers.ScheduledLiveShowDynamicSqlSupport;
import com.project.bookmyshow.db.mappers.ScheduledLiveShowMapper;
import lombok.Cleanup;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

@Repository
public class ScheduledLiveShowDAO {

    public ScheduledLiveShow getScheduledLiveShowById(int scheduledLiveShowId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        ScheduledLiveShowMapper scheduledLiveShowMapper = sqlSession.getMapper(ScheduledLiveShowMapper.class);
        return scheduledLiveShowMapper.selectByPrimaryKey(scheduledLiveShowId);
    }

    public List<ScheduledLiveShow> getAllScheduledLiveShow() {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        ScheduledLiveShowMapper scheduledLiveShowMapper = sqlSession.getMapper(ScheduledLiveShowMapper.class);
        return scheduledLiveShowMapper.selectByExample().build().execute();
    }

    public List<ScheduledLiveShow> getScheduledLiveShowByLiveShowId(int liveShowId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        ScheduledLiveShowMapper scheduledLiveShowMapper = sqlSession.getMapper(ScheduledLiveShowMapper.class);
        ScheduledLiveShowDynamicSqlSupport.ScheduledLiveShow scheduledLiveShowSqlSupport =
                new ScheduledLiveShowDynamicSqlSupport.ScheduledLiveShow();
        return scheduledLiveShowMapper.selectByExample()
                .where(scheduledLiveShowSqlSupport.liveShowId, SqlBuilder.isEqualTo(liveShowId))
                .build().execute();
    }

    public ScheduledLiveShow getScheduledLiveShowBy(int liveShowId, Date startTime, Date endTime) {
        ScheduledLiveShow scheduledLiveShow = null;
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        ScheduledLiveShowMapper scheduledLiveShowMapper = sqlSession.getMapper(ScheduledLiveShowMapper.class);
        ScheduledLiveShowDynamicSqlSupport.ScheduledLiveShow scheduledLiveShowSqlSupport =
                new ScheduledLiveShowDynamicSqlSupport.ScheduledLiveShow();
        List<ScheduledLiveShow> scheduledLiveShowList = scheduledLiveShowMapper.selectByExample()
                .where(scheduledLiveShowSqlSupport.liveShowId, SqlBuilder.isEqualTo(liveShowId))
                .and(scheduledLiveShowSqlSupport.showStartTime, SqlBuilder.isEqualTo(startTime))
                .and(scheduledLiveShowSqlSupport.showEndTime, SqlBuilder.isEqualTo(endTime))
                .limit(1).build().execute();
        if (!CollectionUtils.isEmpty(scheduledLiveShowList)) {
            scheduledLiveShow = scheduledLiveShowList.get(0);
        }
        return scheduledLiveShow;
    }

    public ScheduledLiveShow insertOrUpdateScheduledLiveShowBy(int liveShowId, Date startTime, Date endTime, double ticketPrice) {
        ScheduledLiveShow scheduledLiveShow = getScheduledLiveShowBy(liveShowId, startTime, endTime);
        if (scheduledLiveShow == null) {
            scheduledLiveShow = new ScheduledLiveShow();
            scheduledLiveShow.setLiveShowId(liveShowId);
            scheduledLiveShow.setShowStartTime(startTime);
            scheduledLiveShow.setShowEndTime(endTime);
            scheduledLiveShow.setTicketsPrice(ticketPrice);
            @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
            ScheduledLiveShowMapper scheduledLiveShowMapper = sqlSession.getMapper(ScheduledLiveShowMapper.class);
            scheduledLiveShowMapper.insertSelective(scheduledLiveShow);
            sqlSession.commit();
        }
        return scheduledLiveShow;
    }
}
