package com.project.bookmyshow.db.dao;

import com.project.bookmyshow.db.ConnectionFactory;
import com.project.bookmyshow.db.mappers.Cinema;
import com.project.bookmyshow.db.mappers.CinemaDynamicSqlSupport;
import com.project.bookmyshow.db.mappers.CinemaMapper;
import lombok.Cleanup;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CinemaDAO {

    public Cinema getCinemayId(int cinemaId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        CinemaMapper cinemaMapper = sqlSession.getMapper(CinemaMapper.class);
        return cinemaMapper.selectByPrimaryKey(cinemaId);
    }

    public List<Cinema> getAllCinema() {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        CinemaMapper cinemaMapper = sqlSession.getMapper(CinemaMapper.class);
        CinemaDynamicSqlSupport.Cinema cinemaSqlSupport = new CinemaDynamicSqlSupport.Cinema();
        return cinemaMapper.selectByExample()
                .where(cinemaSqlSupport.isOpen, SqlBuilder.isEqualTo(true))
                .build().execute();
    }
}
