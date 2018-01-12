package com.turding;

import com.turding.sponge.annotation.PK;
import com.turding.sponge.annotation.StoreEntity;
import com.turding.sponge.annotation.StoreField;
import com.turding.sponge.core.Storable;
import lombok.Data;

import java.util.Date;

/**
 * Created by myron.pan on 18-1-12.
 */
@Data
@StoreEntity(storeTarget = "agent_agent_roles")
public class AgentAgentRolesTest implements Storable {

    @PK
    private Integer id;

    @StoreField(storeName = "agent_id")
    private Integer agentId;

    @StoreField(storeName = "agent_role_id")
    private Integer agentRoleId;

    @StoreField(storeName = "created_at")
    private Date createdAt;

    @StoreField(storeName = "updated_at")
    private Date updatedAt;

}
