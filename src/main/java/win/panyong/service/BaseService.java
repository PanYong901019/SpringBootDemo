package win.panyong.service;


import org.springframework.beans.factory.annotation.Autowired;
import win.panyong.dao.mysql.MysqlCommonDao;
import win.panyong.dao.oracle.OracleCommonDao;
import win.panyong.utils.AppCache;
import win.panyong.utils.RedisCache;

public class BaseService {
    @Autowired
    protected AppCache appCache;
    @Autowired
    protected RedisCache redisCache;
    @Autowired
    protected MysqlCommonDao mysqlCommonDaoImpl;
    @Autowired
    protected OracleCommonDao oracleCommonDaoImpl;

}
