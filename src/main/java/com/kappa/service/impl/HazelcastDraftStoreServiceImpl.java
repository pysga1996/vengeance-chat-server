package com.kappa.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.kappa.model.entity.DraftMessageBlock;
import com.kappa.service.DraftStoreService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"default","poweredge"})
@DependsOn({"hazelcastClientInstance"})
public class HazelcastDraftStoreServiceImpl implements DraftStoreService {

    private final HazelcastInstance hazelcastClientInstance;

    @Autowired
    public HazelcastDraftStoreServiceImpl(HazelcastInstance hazelcastClientInstance) {
        this.hazelcastClientInstance = hazelcastClientInstance;
    }

    @Override
    public Map<String, Object> getMap(String mapName) {
        return this.hazelcastClientInstance.getMap(mapName);
    }

    @Override
    public void updateDraft(Map<String, Object> map, String conversationId, DraftMessageBlock draft) throws InterruptedException {
        IMap<String, Object> hazelcastMap = (IMap<String, Object>) map;
        while (hazelcastMap.isLocked(conversationId)) {
            Thread.sleep(5000);
        }

        hazelcastMap.lock(conversationId);
        hazelcastMap.put(conversationId, draft);
        hazelcastMap.unlock(conversationId);
    }

    @Override
    public void clearDraft(Map<String, Object> map, String conversationId) throws InterruptedException {
        IMap<String, Object> hazelcastMap = (IMap<String, Object>) map;
        while (hazelcastMap.isLocked(conversationId)) {
            Thread.sleep(5000);
        }
        hazelcastMap.lock(conversationId);
        hazelcastMap.remove(conversationId);
        hazelcastMap.unlock(conversationId);
    }
}
