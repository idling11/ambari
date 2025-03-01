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
package org.apache.ambari.server.orm.entities;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "groups", uniqueConstraints = {@UniqueConstraint(columnNames = {"group_name", "ldap_group"})})
@TableGenerator(name = "group_id_generator",
    table = "ambari_sequences",
    pkColumnName = "sequence_name",
    valueColumnName = "value",
    pkColumnValue = "group_id_seq",
    initialValue = 1,
    allocationSize = 1
    )
@NamedQueries({
  @NamedQuery(name = "groupByName", query = "SELECT group_entity FROM GroupEntity group_entity where lower(group_entity.groupName)=:groupname")
})
public class GroupEntity {
  @Id
  @Column(name = "group_id")
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "group_id_generator")
  private Integer groupId;

  @Column(name = "group_name")
  private String groupName;

  @Column(name = "ldap_group")
  private Integer ldapGroup = 0;

  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
  private Set<MemberEntity> memberEntities;

  public Integer getGroupId() {
    return groupId;
  }

  public void setGroupId(Integer groupId) {
    this.groupId = groupId;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public Boolean getLdapGroup() {
    return ldapGroup == 0 ? Boolean.FALSE : Boolean.TRUE;
  }

  public void setLdapGroup(Boolean ldapGroup) {
    if (ldapGroup == null) {
      this.ldapGroup = null;
    } else {
      this.ldapGroup = ldapGroup ? 1 : 0;
    }
  }

  public Set<MemberEntity> getMemberEntities() {
    return memberEntities;
  }

  public void setMemberEntities(Set<MemberEntity> memberEntities) {
    this.memberEntities = memberEntities;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GroupEntity that = (GroupEntity) o;

    if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) return false;
    if (groupName != null ? !groupName.equals(that.groupName) : that.groupName != null) return false;
    if (ldapGroup != null ? !ldapGroup.equals(that.ldapGroup) : that.ldapGroup != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = groupId != null ? groupId.hashCode() : 0;
    result = 31 * result + (groupName != null ? groupName.hashCode() : 0);
    result = 31 * result + (ldapGroup != null ? ldapGroup.hashCode() : 0);
    return result;
  }
}
