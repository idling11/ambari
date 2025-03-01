/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.server.orm.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.ambari.server.orm.RequiresSession;
import org.apache.ambari.server.orm.entities.GroupEntity;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class GroupDAO {
  @Inject
  Provider<EntityManager> entityManagerProvider;
  @Inject
  DaoUtils daoUtils;

  @RequiresSession
  public GroupEntity findByPK(Integer groupPK) {
    return entityManagerProvider.get().find(GroupEntity.class, groupPK);
  }

  @RequiresSession
  public List<GroupEntity> findAll() {
    final TypedQuery<GroupEntity> query = entityManagerProvider.get().createQuery("SELECT group_entity FROM GroupEntity group_entity", GroupEntity.class);
    return daoUtils.selectList(query);
  }

  @RequiresSession
  public GroupEntity findGroupByName(String groupName) {
    final TypedQuery<GroupEntity> query = entityManagerProvider.get().createNamedQuery("groupByName", GroupEntity.class);
    query.setParameter("groupname", groupName.toLowerCase());
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Transactional
  public void create(GroupEntity group) {
    group.setGroupName(group.getGroupName().toLowerCase());
    entityManagerProvider.get().persist(group);
  }

  @Transactional
  public GroupEntity merge(GroupEntity group) {
    group.setGroupName(group.getGroupName().toLowerCase());
    return entityManagerProvider.get().merge(group);
  }

  @Transactional
  public void remove(GroupEntity group) {
    entityManagerProvider.get().remove(merge(group));
  }

  @Transactional
  public void removeByPK(Integer groupPK) {
    remove(findByPK(groupPK));
  }
}
