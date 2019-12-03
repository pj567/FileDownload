package com.hzjy.download.db;

import android.content.Context;

import com.hzjy.download.DownloadEntity;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * pj567
 * 2019/11/25
 */

public class DBController {
    private volatile static DBController instance;
    private Context mContext;
    private OrmDBHelper dbHelper;
    private Dao<DownloadEntity, String> dao;

    private DBController(Context context) {
        mContext = context.getApplicationContext();
        dbHelper = new OrmDBHelper(mContext);
    }

    public static DBController getInstance(Context context) {
        if (instance == null) {
            synchronized (DBController.class) {
                if (instance == null) {
                    instance = new DBController(context);
                }
            }
        }
        return instance;
    }

    public synchronized void newOrUpdate(DownloadEntity entity) {
        try {
            dao = dbHelper.getDao(DownloadEntity.class);
            dao.createOrUpdate(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<DownloadEntity> queryAll() {
        try {
            dao = dbHelper.getDao(DownloadEntity.class);
            return dao.query(dao.queryBuilder().prepare());
        } catch (SQLException e) {
            return null;
        }
    }

    public synchronized DownloadEntity queryById(String id) {
        try {
            dao = dbHelper.getDao(DownloadEntity.class);
            return dao.queryForId(id);
        } catch (SQLException e) {
            return null;
        }
    }

    public synchronized void deleteEntity(DownloadEntity entity) {
        try {
            dao = dbHelper.getDao(DownloadEntity.class);
            dao.delete(entity);
        } catch (SQLException e) {
        }
    }
}
