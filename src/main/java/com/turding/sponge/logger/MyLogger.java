package com.turding.sponge.logger;

import com.turding.sponge.core.CombinedExpression;
import com.turding.sponge.core.Storable;

import java.util.List;

/**
 * udesk logger.
 * 用于： 记录、修改、删除 udesk activity log信息
 *
 * Created by yunfeng.pan on 17-6-14.
 */
public interface MyLogger {
    /**
     * 记录日志.
     *
     * @param logEntity
     * @param <T>
     */
    <T extends Storable> void log(T logEntity);

    /**
     * 批量记录日志.
     *
     * @param logEntityList
     * @param <T>
     */
    <T extends Storable> void log(List<T> logEntityList);

    /**
     * 按主键更新记录. 主键为必设参数.
     * @param logEntity
     * @param <T>
     * @return
     */
    <T extends Storable> int updateByPK(T logEntity);

    /**
     * 按条件更新.
     *
     * @param logEntity
     * @param condition
     * @param <T>
     * @return
     */
    <T extends Storable> int update(T logEntity, CombinedExpression condition);

    /**
     * 按主键删除记录.
     *
     * @param logEntity
     * @param <T>
     * @return
     */
    <T extends Storable> int deleteByPK(T logEntity);

    /**
     * 按给定条件删除记录.
     *
     * @param entityClass
     * @param condition
     * @param <T>
     * @return
     */
    <T extends Storable> int delete(Class<T> entityClass, CombinedExpression condition);
}
