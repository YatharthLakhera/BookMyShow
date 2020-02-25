package com.project.bookmyshow.db;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

@Slf4j
public enum ConnectionFactory {

    INSTANCE;

    private SqlSessionFactory sqlSessionManager;

    public void init() throws Exception {
        if (sqlSessionManager == null) {
            synchronized (ConnectionFactory.class) {
                if (sqlSessionManager == null) {
                    String currentProjectPath = System.getProperty("user.dir");
                    String DB_PROPERTIES_FILE = currentProjectPath + "/src/main/resources/db.properties";
                    String MYBATIS_CONFIG_FILE = currentProjectPath + "/src/main/resources/mybatisConfig.xml";
                    String ENVIRONMENT_TYPE = "environmentType";

                    Reader mybatisConfig = new FileReader(new File(MYBATIS_CONFIG_FILE));
                    Properties dbProperties = new Properties();
                    String environmentType = dbProperties.getProperty(ENVIRONMENT_TYPE);
                    dbProperties.load(new FileReader(new File(DB_PROPERTIES_FILE)));
                    sqlSessionManager = new SqlSessionFactoryBuilder().build(mybatisConfig, environmentType, dbProperties);
                }
            }
        }
    }

    public SqlSession getSqlSession() {
        return sqlSessionManager.openSession();
    }
}
