package com.cyanbirds.tanlove.greendao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.cyanbirds.momo.entity.Conversation;
import com.cyanbirds.momo.entity.Dynamic;
import com.cyanbirds.momo.entity.Gold;
import com.cyanbirds.momo.entity.IMessage;
import com.cyanbirds.momo.entity.LocationModel;
import com.cyanbirds.momo.entity.NameList;

import com.cyanbirds.tanlove.greendao.ConversationDao;
import com.cyanbirds.tanlove.greendao.DynamicDao;
import com.cyanbirds.tanlove.greendao.GoldDao;
import com.cyanbirds.tanlove.greendao.IMessageDao;
import com.cyanbirds.tanlove.greendao.LocationModelDao;
import com.cyanbirds.tanlove.greendao.NameListDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig conversationDaoConfig;
    private final DaoConfig dynamicDaoConfig;
    private final DaoConfig goldDaoConfig;
    private final DaoConfig iMessageDaoConfig;
    private final DaoConfig locationModelDaoConfig;
    private final DaoConfig nameListDaoConfig;

    private final ConversationDao conversationDao;
    private final DynamicDao dynamicDao;
    private final GoldDao goldDao;
    private final IMessageDao iMessageDao;
    private final LocationModelDao locationModelDao;
    private final NameListDao nameListDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        conversationDaoConfig = daoConfigMap.get(ConversationDao.class).clone();
        conversationDaoConfig.initIdentityScope(type);

        dynamicDaoConfig = daoConfigMap.get(DynamicDao.class).clone();
        dynamicDaoConfig.initIdentityScope(type);

        goldDaoConfig = daoConfigMap.get(GoldDao.class).clone();
        goldDaoConfig.initIdentityScope(type);

        iMessageDaoConfig = daoConfigMap.get(IMessageDao.class).clone();
        iMessageDaoConfig.initIdentityScope(type);

        locationModelDaoConfig = daoConfigMap.get(LocationModelDao.class).clone();
        locationModelDaoConfig.initIdentityScope(type);

        nameListDaoConfig = daoConfigMap.get(NameListDao.class).clone();
        nameListDaoConfig.initIdentityScope(type);

        conversationDao = new ConversationDao(conversationDaoConfig, this);
        dynamicDao = new DynamicDao(dynamicDaoConfig, this);
        goldDao = new GoldDao(goldDaoConfig, this);
        iMessageDao = new IMessageDao(iMessageDaoConfig, this);
        locationModelDao = new LocationModelDao(locationModelDaoConfig, this);
        nameListDao = new NameListDao(nameListDaoConfig, this);

        registerDao(Conversation.class, conversationDao);
        registerDao(Dynamic.class, dynamicDao);
        registerDao(Gold.class, goldDao);
        registerDao(IMessage.class, iMessageDao);
        registerDao(LocationModel.class, locationModelDao);
        registerDao(NameList.class, nameListDao);
    }
    
    public void clear() {
        conversationDaoConfig.clearIdentityScope();
        dynamicDaoConfig.clearIdentityScope();
        goldDaoConfig.clearIdentityScope();
        iMessageDaoConfig.clearIdentityScope();
        locationModelDaoConfig.clearIdentityScope();
        nameListDaoConfig.clearIdentityScope();
    }

    public ConversationDao getConversationDao() {
        return conversationDao;
    }

    public DynamicDao getDynamicDao() {
        return dynamicDao;
    }

    public GoldDao getGoldDao() {
        return goldDao;
    }

    public IMessageDao getIMessageDao() {
        return iMessageDao;
    }

    public LocationModelDao getLocationModelDao() {
        return locationModelDao;
    }

    public NameListDao getNameListDao() {
        return nameListDao;
    }

}
