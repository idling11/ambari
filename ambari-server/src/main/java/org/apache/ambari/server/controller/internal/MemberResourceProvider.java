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
package org.apache.ambari.server.controller.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.MemberRequest;
import org.apache.ambari.server.controller.MemberResponse;
import org.apache.ambari.server.controller.spi.NoSuchParentResourceException;
import org.apache.ambari.server.controller.spi.NoSuchResourceException;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.RequestStatus;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceAlreadyExistsException;
import org.apache.ambari.server.controller.spi.SystemException;
import org.apache.ambari.server.controller.spi.UnsupportedPropertyException;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.persist.Transactional;

/**
 * Resource provider for member resources.
 */
public class MemberResourceProvider extends AbstractControllerResourceProvider {

  // ----- Property ID constants ---------------------------------------------

  // Members
  protected static final String MEMBER_GROUP_NAME_PROPERTY_ID = PropertyHelper.getPropertyId("MemberInfo", "group_name");
  protected static final String MEMBER_USER_NAME_PROPERTY_ID  = PropertyHelper.getPropertyId("MemberInfo", "user_name");

  private static Set<String> pkPropertyIds =
      new HashSet<String>(Arrays.asList(new String[]{
          MEMBER_GROUP_NAME_PROPERTY_ID,
          MEMBER_USER_NAME_PROPERTY_ID}));

  /**
   * Create a  new resource provider for the given management controller.
   *
   * @param propertyIds           the property ids
   * @param keyPropertyIds        the key property ids
   * @param managementController  the management controller
   */
  @AssistedInject
  public MemberResourceProvider(@Assisted Set<String> propertyIds,
                          @Assisted Map<Resource.Type, String> keyPropertyIds,
                          @Assisted AmbariManagementController managementController) {
    super(propertyIds, keyPropertyIds, managementController);
  }

  @Override
  public RequestStatus createResources(Request request)
      throws SystemException,
             UnsupportedPropertyException,
             ResourceAlreadyExistsException,
             NoSuchParentResourceException {

    final Set<MemberRequest> requests = new HashSet<MemberRequest>();
    for (Map<String, Object> propertyMap : request.getProperties()) {
      requests.add(getRequest(propertyMap));
    }
    createResources(new Command<Void>() {
      @Override
      public Void invoke() throws AmbariException {
        getManagementController().createMembers(requests);
        return null;
      }
    });

    return getRequestStatus(null);
  }

  @Override
  @Transactional
  public Set<Resource> getResources(Request request, Predicate predicate) throws
      SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {

    final Set<MemberRequest> requests = new HashSet<MemberRequest>();
    for (Map<String, Object> propertyMap : getPropertyMaps(predicate)) {
      requests.add(getRequest(propertyMap));
    }

    Set<MemberResponse> responses = getResources(new Command<Set<MemberResponse>>() {
      @Override
      public Set<MemberResponse> invoke() throws AmbariException {
        return getManagementController().getMembers(requests);
      }
    });

    LOG.debug("Found member responses matching get members request"
        + ", membersRequestSize=" + requests.size() + ", membersResponseSize="
        + responses.size());

    Set<String>   requestedIds = getRequestPropertyIds(request, predicate);
    Set<Resource> resources    = new HashSet<Resource>();

    for (MemberResponse memberResponse : responses) {
      ResourceImpl resource = new ResourceImpl(Resource.Type.Member);

      setResourceProperty(resource, MEMBER_GROUP_NAME_PROPERTY_ID,
          memberResponse.getGroupName(), requestedIds);

      setResourceProperty(resource, MEMBER_USER_NAME_PROPERTY_ID,
          memberResponse.getUserName(), requestedIds);

      resources.add(resource);
    }

    return resources;
  }

  @Override
  public RequestStatus updateResources(final Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {

    final Set<MemberRequest> requests = new HashSet<MemberRequest>();
    for (Map<String, Object> propertyMap : request.getProperties()) {
      requests.add(getRequest(propertyMap));
    }

    modifyResources(new Command<Void>() {
      @Override
      public Void invoke() throws AmbariException {
        // do nothing
        return null;
      }
    });

    return getRequestStatus(null);
  }

  @Override
  public RequestStatus deleteResources(Predicate predicate)
      throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {

    final Set<MemberRequest> requests = new HashSet<MemberRequest>();

    for (Map<String, Object> propertyMap : getPropertyMaps(predicate)) {
      final MemberRequest req = getRequest(propertyMap);
      requests.add(req);
    }

    modifyResources(new Command<Void>() {
      @Override
      public Void invoke() throws AmbariException {
        getManagementController().deleteMembers(requests);
        return null;
      }
    });

    return getRequestStatus(null);
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return pkPropertyIds;
  }

  private MemberRequest getRequest(Map<String, Object> properties) {
    if (properties == null) {
      return new MemberRequest(null, null);
    }

    final MemberRequest request = new MemberRequest(
        (String) properties.get(MEMBER_GROUP_NAME_PROPERTY_ID),
        (String) properties.get(MEMBER_USER_NAME_PROPERTY_ID));
    return request;
  }
}
