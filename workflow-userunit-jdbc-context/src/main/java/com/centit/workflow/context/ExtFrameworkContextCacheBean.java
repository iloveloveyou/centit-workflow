package com.centit.workflow.context;

import com.centit.framework.components.CodeRepositoryCache;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.core.dao.ExtendedQueryPool;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.common.CachedMap;
import com.centit.support.common.CachedObject;
import com.centit.support.database.utils.DataSourceDescription;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.DbcpConnectPools;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-9-11.
 * 在这个过滤器中 用户信息只需要用户代码、用户默认机构
 * 机构信息中只需要机构代码、
 * 机构人员信息中需要 用户代码、机构代码、 用户位和职务
 * Rank 获取用户职务等级信息，有一个职务类表
 */

public class ExtFrameworkContextCacheBean {

    //@Value("${wf.external.system.jdbc.url:}")
    protected String externalJdbcUrl;
    //@Value("${wf.external.system.jdbc.user:}")
    protected String externalJdbcUser;
    //@Value("${wf.external.system.jdbc.password:}")
    protected String externalJdbcPassword;


    protected static final Logger logger = LoggerFactory.getLogger(ExtFrameworkContextCacheBean.class);

    public void setDatabaseSource(String dbUrl, String userName, String password){
        this.externalJdbcUrl = dbUrl;
        this.externalJdbcUser = userName;
        this.externalJdbcPassword = password;
    }

    CachedObject<List<ExtSysUserInfo>> allUserInfoCache =
        new CachedObject<>( this::reloadUserInfo,
            CodeRepositoryCache.CACHE_FRESH_PERIOD_SECONDS);

    CachedObject<Map<String,ExtSysUserInfo>> codeToUserMapCache=
        new CachedObject<>( ()-> {
                List<ExtSysUserInfo> userInfos = allUserInfoCache.getCachedTarget();
                if(userInfos==null){
                    return null;
                }
                Map<String, ExtSysUserInfo> userCodeMap = new HashMap<>(userInfos.size()+1);
                for(ExtSysUserInfo userInfo : userInfos ){
                    userCodeMap.put(userInfo.getUserCode(), userInfo);
                }
                return userCodeMap;
            },
            allUserInfoCache);

    CachedObject<List<ExtSysUnitInfo>> allunitInfoCache=
        new CachedObject<>( this::reloadUnitInfo,
            CodeRepositoryCache.CACHE_FRESH_PERIOD_SECONDS);

    private List<ExtSysUnitInfo> fetchSubUnit(String unitCode) {
        List<ExtSysUnitInfo> units = new ArrayList<>(10);
        for(ExtSysUnitInfo ui : allunitInfoCache.getCachedTarget() ){
            if(StringUtils.equals(unitCode,ui.getParentUnit()) ||
                ("null".equals(unitCode) && StringUtils.isBlank(ui.getParentUnit()))){
                units.add(ui);
            }
        }
        return units;
    }

    CachedMap<String, List<ExtSysUnitInfo>> subUnitMapCache=
        new CachedMap<>( this::fetchSubUnit,
            allunitInfoCache);

    CachedObject<Map<String,ExtSysUnitInfo>> codeToUnitMapCache=
        new CachedObject<>( ()-> {
                List<ExtSysUnitInfo> unitInfos = allunitInfoCache.getCachedTarget();
                if(unitInfos==null){
                    return null;
                }
                Map<String, ExtSysUnitInfo> userCodeMap = new HashMap<>(unitInfos.size()+1);
                for(ExtSysUnitInfo unitInfo : unitInfos ){
                    userCodeMap.put(unitInfo.getUnitCode(), unitInfo);
                }
                return userCodeMap;
            },
            allunitInfoCache);


    CachedObject<List<ExtSysUserUnit>> allUserUnitCache =
        new CachedObject<>( this::reloadUserUnit,
            CodeRepositoryCache.CACHE_FRESH_PERIOD_SECONDS);

    CachedMap<String, List<ExtSysUserUnit>> userUnitMapCache=
        new CachedMap<>( (userCode) ->{
                List<ExtSysUserUnit> allUserUnits = allUserUnitCache.getCachedTarget();
                if(allUserUnits==null)
                    return null;
                List<ExtSysUserUnit> userUnits = new ArrayList<>(4);
                for(ExtSysUserUnit uu : allUserUnits){
                    if(StringUtils.equals(userCode, uu.getUserCode())){
                        userUnits.add(uu);
                    }
                }
                return userUnits;
            },
            allUserUnitCache);

    CachedMap<String, List<ExtSysUserUnit>> unitUserMapCache=
        new CachedMap<>( (unitCode) ->{
                List<ExtSysUserUnit> allUserUnits = allUserUnitCache.getCachedTarget();
                if(allUserUnits==null)
                    return null;
                List<ExtSysUserUnit> unitUsers = new ArrayList<>(20);
                for(ExtSysUserUnit uu : allUserUnits){
                    if(StringUtils.equals(unitCode, uu.getUnitCode())){
                        unitUsers.add(uu);
                    }
                }
                return unitUsers;
            },
            allUserUnitCache);
    // ------------------- user Role ------------------------------------
    CachedObject<List<ExtSysUserRole>> allUserRoleCache =
        new CachedObject<>( this::reloadUserRole,
            CodeRepositoryCache.CACHE_FRESH_PERIOD_SECONDS);

    CachedMap<String, List<ExtSysUserRole>> userRoleMapCache=
        new CachedMap<>( (userCode) ->{
            List<ExtSysUserRole> userRoleList = allUserRoleCache.getCachedTarget();
            if(userRoleList==null)
                return null;
            List<ExtSysUserRole> userRoles = new ArrayList<>(4);
            for(ExtSysUserRole uo : userRoleList){
                if(StringUtils.equals(userCode, uo.getUserCode())){
                    userRoles.add(uo);
                }
            }
            return userRoles;
        }, allUserRoleCache);

    CachedMap<String, List<ExtSysUserRole>> roleUserMapCache=
        new CachedMap<>( (roleCode) ->{
            List<ExtSysUserRole> userRoleList = allUserRoleCache.getCachedTarget();
            if(userRoleList==null)
                return null;
            List<ExtSysUserRole> userRoles = new ArrayList<>(20);
            for(ExtSysUserRole uo : userRoleList){
                if(StringUtils.equals(roleCode, uo.getRoleCode())){
                    userRoles.add(uo);
                }
            }
            return userRoles;
        }, allUserRoleCache);


    CachedObject<List<Triple<String, String, Integer>>> rankInfoCache =
        new CachedObject<>( this::reloadAllRank,
            CodeRepositoryCache.CACHE_FRESH_PERIOD_SECONDS);

    CachedObject<Map<String, String>> systemRoleMapCache =
        new CachedObject<>( this::reloadAllSystemRole,
            CodeRepositoryCache.CACHE_FRESH_PERIOD_SECONDS);

    CachedObject<Map<String, String>> projectRoleMapCache =
        new CachedObject<>( this::reloadAllProjectRole,
            CodeRepositoryCache.CACHE_FRESH_PERIOD_SECONDS);

    CachedObject<Map<String, String>> stationMapCache =
        new CachedObject<>( this::reloadAllStation,
            CodeRepositoryCache.CACHE_FRESH_PERIOD_SECONDS);

    public ExtSysUnitInfo searchUnitInfoByCode(String unitCode) {
        for(ExtSysUnitInfo unitInfo : allunitInfoCache.getCachedTarget()){
            if(unitInfo.getUnitCode().equals(unitCode))
                return unitInfo;
        }
        return null;
    }

    public ExtSysUserInfo searchUserInfoByCode(String userCode) {
        for(ExtSysUserInfo userInfo : allUserInfoCache.getCachedTarget()){
            if(userInfo.getUserCode().equals(userCode))
                return userInfo;
        }
        return null;
    }


    private Connection getExternalDataConnection() throws SQLException {
        DataSourceDescription dataSourceDesc = new DataSourceDescription();
        dataSourceDesc.setConnUrl(externalJdbcUrl);//SysParametersUtils.getStringValue("wf.external.system.jdbc.url"));
        dataSourceDesc.setUsername(externalJdbcUser);//SysParametersUtils.getStringValue("wf.external.system.jdbc.user"));
        dataSourceDesc.setPassword(externalJdbcPassword);//SysParametersUtils.getStringValue("wf.external.system.jdbc.password"));
        return DbcpConnectPools.getDbcpConnect(dataSourceDesc);
    }


    protected List<ExtSysUserInfo> reloadUserInfo() {
        try(Connection conn = getExternalDataConnection() ) {
            List<Object[]> users = DatabaseAccess.findObjectsBySql(conn,
                ExtendedQueryPool.getExtendedSql("WORKFLOW_EXTERNAL_USERINFO"));
            if (users == null)
                return null;
            List<ExtSysUserInfo> allUserInfo = new ArrayList<>(users.size() + 1);
            for (Object[] user : users) {
                ExtSysUserInfo userInfo = new ExtSysUserInfo();
                userInfo.setUserCode(StringBaseOpt.objectToString(user[0]));
                userInfo.setUserName(StringBaseOpt.objectToString(user[1]));
                userInfo.setPrimaryUnit(StringBaseOpt.objectToString(user[2]));
                userInfo.setUserOrder(NumberBaseOpt.castObjectToLong(user[3]));

                allUserInfo.add(userInfo);
            }
            return allUserInfo;
        }catch (SQLException |IOException  e){
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }


    protected List<ExtSysUnitInfo> reloadUnitInfo(){
        try(Connection conn = getExternalDataConnection() ) {
            List<Object[]> units =DatabaseAccess.findObjectsBySql(conn,
                ExtendedQueryPool.getExtendedSql("WORKFLOW_EXTERNAL_UNITINFO") );

            if(units == null)
                return null;
            List<ExtSysUnitInfo>  allunitInfo = new ArrayList<>(units.size()+1);
            for(Object[] unit : units){
                ExtSysUnitInfo unitInfo = new ExtSysUnitInfo();
                unitInfo.setUnitCode(StringBaseOpt.objectToString(unit[0]));
                unitInfo.setParentUnit(StringBaseOpt.objectToString(unit[1]));
                unitInfo.setUnitName(StringBaseOpt.objectToString(unit[2]));
                unitInfo.setUnitManager(StringBaseOpt.objectToString(unit[3]));
                unitInfo.setUnitOrder(NumberBaseOpt.castObjectToLong(unit[4]));
                unitInfo.setUnitPath(StringBaseOpt.objectToString(unit[5]));

                allunitInfo.add(unitInfo);
            }
            return allunitInfo;
        }catch (SQLException |IOException  e){
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }

    protected List<ExtSysUserUnit> reloadUserUnit(){
        try(Connection conn = getExternalDataConnection() ) {
            List<Object[]> userUnits = DatabaseAccess.findObjectsBySql(conn,
                ExtendedQueryPool.getExtendedSql("WORKFLOW_EXTERNAL_USERUNIT") );

            if(userUnits == null)
                return null;
            List<ExtSysUserUnit>  allUserUnits = new ArrayList<>(userUnits.size()+1);
            for(Object[] uu: userUnits){
                ExtSysUserUnit userUnit = new ExtSysUserUnit();
                userUnit.setUnitCode(StringBaseOpt.objectToString(uu[0]));
                userUnit.setUserCode(StringBaseOpt.objectToString(uu[1]));
                userUnit.setUserStation(StringBaseOpt.objectToString(uu[2]));
                userUnit.setUserRank(StringBaseOpt.objectToString(uu[3]));
                userUnit.setIsPrimary(StringBaseOpt.objectToString(uu[4]));
                userUnit.setUserOrder(NumberBaseOpt.castObjectToLong(uu[5]));
                allUserUnits.add(userUnit);
            }
            return allUserUnits;
        }catch (SQLException |IOException  e){
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }

    protected List<ExtSysUserRole> reloadUserRole(){
        try(Connection conn = getExternalDataConnection() ) {
            List<Object[]> userRoles = DatabaseAccess.findObjectsBySql(conn,
                ExtendedQueryPool.getExtendedSql("WORKFLOW_EXTERNAL_USERROLE") );

            if(userRoles == null)
                return null;
            List<ExtSysUserRole>  allUserRoles = new ArrayList<>(userRoles.size()+1);
            for(Object[] uo: userRoles){
                ExtSysUserRole role = new ExtSysUserRole();
                role.setUserCode(StringBaseOpt.objectToString(uo[0]));
                role.setRoleCode(StringBaseOpt.objectToString(uo[1]));
                role.setObtainType(StringBaseOpt.objectToString(uo[2]));
                role.setInheritedFrom(StringBaseOpt.objectToString(uo[3]));
                allUserRoles.add(role);
            }
            return allUserRoles;
        }catch (SQLException |IOException  e){
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }
    private Map<String, String> reloadMapBySqlId(String sqlId){
        try(Connection conn = getExternalDataConnection() ) {
            List<Object[]> objects = DatabaseAccess.findObjectsBySql(conn,
                ExtendedQueryPool.getExtendedSql(sqlId) );

            if(objects == null)
                return null;
            Map<String, String> objMap = new HashMap<>(objects.size()*2+1);
            for(Object[] rank: objects){
                objMap.put(StringBaseOpt.objectToString(rank[0]),
                    StringBaseOpt.objectToString(rank[1]) );
            }
            return objMap;
        }catch (SQLException |IOException  e){
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }


    protected Map<String, String> reloadAllSystemRole() {
        return reloadMapBySqlId("WORKFLOW_EXTERNAL_ROLE_INFO");
    }

    protected Map<String, String> reloadAllStation() {
        return reloadMapBySqlId("WORKFLOW_EXTERNAL_STATION_INFO");
    }

    protected Map<String, String> reloadAllProjectRole() {
        return reloadMapBySqlId("WORKFLOW_EXTERNAL_ITEM_ROLE");
    }

    protected List<Triple<String, String, Integer>> reloadAllRank() {
        try(Connection conn = getExternalDataConnection() ) {
            List<Object[]> objects = DatabaseAccess.findObjectsBySql(conn,
                ExtendedQueryPool.getExtendedSql("WORKFLOW_EXTERNAL_RANK_INFO") );

            if(objects == null)
                return null;
            List<Triple<String, String, Integer>> ranks  = new ArrayList<>(objects.size()+1);
            for(Object[] rank: objects){
                ranks.add(Triple.of(StringBaseOpt.objectToString(rank[0]),
                    StringBaseOpt.objectToString(rank[1]),
                    NumberBaseOpt.castObjectToInteger(rank[2], CodeRepositoryUtil.MAXXZRANK)));
            }
            return ranks;
        }catch (SQLException |IOException  e){
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }

}
