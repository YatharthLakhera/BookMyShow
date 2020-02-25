package com.project.bookmyshow.db.dao;

import com.project.bookmyshow.db.ConnectionFactory;
import com.project.bookmyshow.db.mappers.Hall;
import com.project.bookmyshow.db.mappers.HallDynamicSqlSupport;
import com.project.bookmyshow.db.mappers.HallMapper;
import lombok.Cleanup;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Repository
public class HallDAO {

    public Hall getHallById(int hallId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        HallMapper hallMapper = sqlSession.getMapper(HallMapper.class);
        return hallMapper.selectByPrimaryKey(hallId);
    }

    public List<Hall> getAllHall() {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        HallMapper hallMapper = sqlSession.getMapper(HallMapper.class);
        return hallMapper.selectByExample().build().execute();
    }

    public List<Hall> getHallsByCinemaId(int cinemaId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        HallMapper hallMapper = sqlSession.getMapper(HallMapper.class);
        HallDynamicSqlSupport.Hall hallSqlSupport = new HallDynamicSqlSupport.Hall();
        return hallMapper.selectByExample()
                .where(hallSqlSupport.cinemaId, SqlBuilder.isEqualTo(cinemaId))
                .and(hallSqlSupport.isAvailable, SqlBuilder.isEqualTo(true))
                .build().execute();
    }

    public Hall getHallBy(int cinemaId, int hallId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        HallMapper hallMapper = sqlSession.getMapper(HallMapper.class);
        HallDynamicSqlSupport.Hall hallSqlSupport = new HallDynamicSqlSupport.Hall();
        List<Hall> hallList = hallMapper.selectByExample()
                .where(hallSqlSupport.cinemaId, SqlBuilder.isEqualTo(cinemaId))
                .and(hallSqlSupport.hallId, SqlBuilder.isEqualTo(hallId))
                .and(hallSqlSupport.isAvailable, SqlBuilder.isEqualTo(true))
                .limit(1).build().execute();
        Hall hall = null;
        if (!CollectionUtils.isEmpty(hallList)) {
            hall = hallList.get(0);
        }
        return hall;
    }
}
