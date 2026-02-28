package win.panyong.service;

import org.springframework.beans.factory.annotation.Autowired;
import win.panyong.mapper.sqlite.SqliteSystemMapper;

public class BaseService {

    @Autowired
    protected SqliteSystemMapper systemMapper;

}
